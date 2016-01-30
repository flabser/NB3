package kz.flabs.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IUsersActivity;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.PortalException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.ServerException;
import kz.flabs.exception.ServerExceptionType;
import kz.flabs.exception.TransformatorException;
import kz.flabs.exception.ViewException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.exception.XSLTFileNotFoundException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.ComplexKeyParser;
import kz.flabs.parser.FilterParser;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.Content;
import kz.flabs.runtimeobj.DocumentForm;
import kz.flabs.runtimeobj.FTSearchRequest;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.Form;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.View;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.outline.Outline;
import kz.flabs.runtimeobj.outline.SearchOutline;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.runtimeobj.queries.Query;
import kz.flabs.runtimeobj.queries.QueryFactory;
import kz.flabs.scriptprocessor.handler.HandlerScriptProcessor;
import kz.flabs.scriptprocessor.oldhandler.HandlerLauncher;
import kz.flabs.scriptprocessor.page.async.DoAsyncProcessor;
import kz.flabs.servlets.admin.ServiceHandler;
import kz.flabs.servlets.sitefiles.AttachmentHandler;
import kz.flabs.servlets.sitefiles.AttachmentHandlerException;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.flabs.util.XMLResponse;
import kz.flabs.webrule.IRule;
import kz.flabs.webrule.Skin;
import kz.flabs.webrule.StaticContentRule;
import kz.flabs.webrule.constants.RuleType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.FormRule;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.outline.OutlineRule;
import kz.flabs.webrule.page.PageRule;
import kz.flabs.webrule.query.QueryRule;
import kz.flabs.webrule.view.ViewRule;
import kz.nextbase.script._Exception;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;
import net.sf.saxon.s9api.SaxonApiException;

import com.google.gson.Gson;

public class Provider extends HttpServlet implements Const {
	private static final long serialVersionUID = 2352885167311108325L;
	private AppEnv env;
	private ServletContext context;

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			context = config.getServletContext();
			env = (AppEnv) context.getAttribute("portalenv");
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		// long start_time = System.currentTimeMillis();
		HttpSession jses = null;
		UserSession userSession = null;
		ProviderResult result = null;
		String disposition = null;
		AttachmentHandler attachHandler = null;

		try {
			request.setCharacterEncoding("utf-8");
			String type = request.getParameter("type");
			String id = request.getParameter("id");
			String key = request.getParameter("key");
			String onlyXML = request.getParameter("onlyxml");

			if (env != null) {
				if (id != null) {
					if (type == null) {
						type = "page";
					}
					IRule rule = env.ruleProvider.getRule(type, id);

					if (rule != null && rule.isAnonymousAccessAllowed()) {
						jses = request.getSession(true);
						try {
							userSession = new UserSession(context, request, response, jses);
						} catch (AuthFailedException e) {
							userSession = new UserSession(new User("anonymous", env), request);
						}
					} else {
						jses = request.getSession(false);
						if (jses == null) {
							jses = request.getSession(true);
							userSession = new UserSession(context, request, response, jses);
							jses.setAttribute("usersession", userSession);
							Environment.addSession(userSession);
						} else {
							userSession = (UserSession) jses.getAttribute("usersession");
							if (userSession == null) {
								userSession = new UserSession(context, request, response, jses);
								jses.setAttribute("usersession", userSession);

							}
						}

					}

					if (type.equals("json")) {
						result = json(userSession, id);
					} else if (type.equals("static")) {
						result = content(request, response, rule, userSession, id);
					} else if (type.equalsIgnoreCase("outline")) {
						result = outline(request, rule, userSession, id);
					} else if (type.equalsIgnoreCase("view")) {
						result = view(request, response, rule, userSession, id);
					} else if (type.equalsIgnoreCase("filter")) {
						result = view(request, response, rule, userSession, id);
					} else if (type.equalsIgnoreCase("page")) {
						result = page(response, request, rule, userSession);
						if (result.publishAs == PublishAsType.OUTPUTSTREAM) {
							attachHandler = new AttachmentHandler(request, response, true);
						}
						// return;
					} else if (type.equalsIgnoreCase("query")) {
						result = query(request, rule, userSession);
					} else if (type.equalsIgnoreCase("count")) {
						result = count(request, rule, userSession);
					} else if (type.equalsIgnoreCase("search")) {
						result = search(request, userSession);
					} else if (type.equalsIgnoreCase("edit")) {
						result = edit(request, rule, userSession, key);
					} else if (type.equalsIgnoreCase("document") || type.equals("glossary")) {
						result = document(request, response, rule, userSession, key, id, type);
					} else if (type.equalsIgnoreCase("structure")) {
						result = structure(request, response, rule, userSession, key, id);
					} else if (type.equalsIgnoreCase("forum")) {
						result = forum(request, response, rule, userSession, key, id);
					} else if (type.equalsIgnoreCase("save")) {
						result = save(request, response, rule, userSession, key);
					} else if (type.equalsIgnoreCase("getattach")) {
						result = getAttach(request, userSession, key);
						attachHandler = new AttachmentHandler(request, response, true);
					} else if (type.equals("delete")) {
						result = delete(request, userSession);
					} else if (type.equals("undelete")) {
						result = undelete(request, userSession);
					} else if (type.equals("handler")) {
						result = handler(request, rule, userSession);
						if (result.publishAs == PublishAsType.OUTPUTSTREAM) {
							attachHandler = new AttachmentHandler(request, response, false);
						}
					} else if (type.equalsIgnoreCase("service")) {
						result = service(request, response, userSession, key, rule);
					} else {
						String reqEnc = request.getCharacterEncoding();
						type = new String(type.getBytes("ISO-8859-1"), reqEnc);
						new PortalException("Request has been undefined, type=" + type + ", id=" + id + ", key=" + key, env, response,
						        ProviderExceptionType.PROVIDERERROR, PublishAsType.HTML, userSession.skin);
						return;
					}

					if (userSession.browserType == BrowserType.APPLICATION && result.publishAs != PublishAsType.OUTPUTSTREAM || onlyXML != null) {
						result.publishAs = PublishAsType.XML;
						result.addHistory = false;
					}
					response.setStatus(result.httpStatus);

					if (result.publishAs == PublishAsType.JSON) {
						response.setContentType("application/json;charset=utf-8");
						PrintWriter out = response.getWriter();
						Gson gson = new Gson();
						String json = gson.toJson(result.jsonOutput);
						System.out.println(json);
						out.println(json);
						out.close();
					} else if (result.publishAs == PublishAsType.HTML) {
						if (result.disableClientCache) {
							disableCash(response);
						}
						ProviderOutput po = new ProviderOutput(type, id, result.output, request, response, userSession, jses, result.title,
						        result.addHistory);
						response.setContentType("text/html");
						Skin skin = null;
						if (po.browser == BrowserType.IPAD_SAFARI || po.browser == BrowserType.ANDROID) {
							skin = env.globalSetting.skinsMap.get("ipadandtab");
						} else {
							skin = env.globalSetting.skinsMap.get(userSession.skin);
						}
						if (po.prepareXSLT(env, skin, result.xslt)) {
							String outputContent = po.getStandartOutput();
							// long start_time = System.currentTimeMillis(); //
							// for speed debuging
							new SaxonTransformator().toTrans(response, po.xslFile, outputContent);
							// System.out.println(getClass().getSimpleName() + "
							// transformation >>> " +
							// Util.getTimeDiffInMilSec(start_time)); // for
							// speed debuging
						} else {
							String outputContent = po.getStandartOutput();
							response.setContentType("text/xml;charset=utf-8");
							PrintWriter out = response.getWriter();
							out.println(outputContent);
							out.close();
						}
					} else if (result.publishAs == PublishAsType.XML) {
						if (result.disableClientCache) {
							disableCash(response);
						}
						response.setContentType("text/xml;charset=utf-8");
						ProviderOutput po = new ProviderOutput(type, id, result.output, request, response, userSession, jses, result.title,
						        result.addHistory);
						String outputContent = po.getStandartUTF8Output();
						// System.out.println(outputContent);
						PrintWriter out = response.getWriter();
						out.println(outputContent);
						out.close();
					} else if (result.publishAs == PublishAsType.TEXT) {
						if (result.disableClientCache) {
							disableCash(response);
						}
						ProviderOutput po = new ProviderOutput(type, id, result.output, request, response, userSession, jses, result.title,
						        result.addHistory);
						String outputContent = po.getPlainText();
						response.setContentType("text/text;charset=utf-8");
						response.getWriter().println(outputContent);
					} else if (result.publishAs == PublishAsType.OUTPUTSTREAM) {
						if (request.getParameter("disposition") != null) {
							disposition = request.getParameter("disposition");
						} else {
							disposition = "attachment";
						}
						attachHandler.publish(result.filePath, result.originalAttachName, disposition);
					} else if (result.publishAs == PublishAsType.FORWARD) {
						response.sendRedirect(result.forwardTo);
						return;
					}
					// System.out.println(type + " " +
					// Util.getTimeDiffInMilSec(start_time));
				} else {
					response.sendRedirect(env.globalSetting.defaultRedirectURL);
					return;
				}
			} else {
				throw new ServerException(ServerExceptionType.APPENV_HAS_NOT_INITIALIZED, "context=" + context.getServletContextName());
			}
		} catch (AuthFailedException e) {
			String referer = request.getRequestURI() + "?" + request.getQueryString();

			if (Environment.workspaceAuth) {
				Cookie cp = new Cookie("calling_page", referer);
				cp.setMaxAge(360);
				cp.setPath("/");
				response.addCookie(cp);
			} else {
				if (jses == null) {
					jses = request.getSession(true);
				}
				jses.setAttribute("callingPage", referer);
			}

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			try {
				if (e.type == AuthFailedExceptionType.NO_USER_SESSION) {
					response.sendRedirect("Logout");
				} else if (e.type == AuthFailedExceptionType.UNANSWERED_QUESTION) {
					// response.sendRedirect("Error?type=ws_auth_error_caused_unanswered_question");
					request.getRequestDispatcher("/Error?type=ws_auth_error_caused_unanswered_question").forward(request, response);
				} else {
					// response.sendRedirect("Error?type=ws_auth_error");
					request.getRequestDispatcher("/Error?type=ws_auth_error").forward(request, response);
				}

			} catch (IOException e1) {
				new PortalException(e, env, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML, userSession.skin);
			} catch (ServletException e2) {
				new PortalException(e2, env, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML, userSession.skin);
			}

		} catch (DocumentAccessException dae) {
			new PortalException(dae, env, response, ProviderExceptionType.DOCUMENTEXCEPTION, PublishAsType.HTML, userSession.skin);
		} catch (RuleException rnf) {
			new PortalException(rnf, env, response, ProviderExceptionType.RULENOTFOUND);
		} catch (ViewException sdre) {
			new PortalException(sdre, env, response, PublishAsType.HTML, userSession.skin);
		} catch (XSLTFileNotFoundException xfnf) {
			new PortalException(xfnf, env, response, ProviderExceptionType.XSLTNOTFOUND, PublishAsType.HTML, userSession.skin);
		} catch (FTIndexEngineException e) {
			new PortalException(e, env, response, ProviderExceptionType.DATAENGINERROR, PublishAsType.HTML, userSession.skin);
		} catch (IOException ioe) {
			new PortalException(ioe, env, response, PublishAsType.HTML, userSession.skin);
		} catch (IllegalStateException ise) {
			new PortalException(ise, env, response, PublishAsType.HTML, userSession.skin);
		} catch (AttachmentHandlerException e) {
			new PortalException(e, env, response, ProviderExceptionType.PROVIDERERROR, PublishAsType.HTML, userSession.skin);
		} catch (UserException e) {
			new PortalException(e, env, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML, userSession.skin);
		} catch (SaxonApiException e) {
			Server.logger.errorLogEntry(e);
			// new PortalException(e, env, response,
			// ProviderExceptionType.XSLT_TRANSFORMATOR_ERROR,
			// PublishAsType.HTML, userSession.skin);
		} catch (TransformatorException e) {
			new PortalException(e, env, response, ProviderExceptionType.XSLT_TRANSFORMATOR_ERROR, PublishAsType.HTML, userSession.skin);
		} catch (ClassNotFoundException e) {
			new PortalException(e, env, response, ProviderExceptionType.CLASS_NOT_FOUND_EXCEPTION, PublishAsType.HTML, userSession.skin);
		} catch (WebFormValueException e) {
			new PortalException(e, env, response, ProviderExceptionType.WEBFORM_VALUE_EXCEPTION, PublishAsType.HTML, userSession.skin);
		} catch (ServerException e) {
			new PortalException(e, response, ProviderExceptionType.SERVER, PublishAsType.HTML);
		} catch (Exception e) {
			new PortalException(e, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML);
		}

		// System.out.println("done...");

	}

	@Deprecated
	private ProviderResult json(UserSession userSession, String id) throws RuleException, DocumentException, DocumentAccessException,
	        QueryFormulaParserException, QueryException, LocalizatorException, ClassNotFoundException {
		ProviderResult result = new ProviderResult();
		result.publishAs = PublishAsType.JSON;
		DoAsyncProcessor dap = new DoAsyncProcessor(userSession);
		result.output.append(dap.processScript(id).toString());
		return result;
	}

	@Deprecated
	private ProviderResult content(HttpServletRequest request, HttpServletResponse response, IRule rule, UserSession userSession, String id)
	        throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException {
		StaticContentRule staticContentRule = (StaticContentRule) rule;
		ProviderResult result = new ProviderResult(staticContentRule.publishAs, staticContentRule.getXSLT());

		if (id.equalsIgnoreCase("start")) {
			if (Environment.workspaceAuth) {
				result.forwardTo = Environment.getWorkspaceURL();
				result.publishAs = PublishAsType.FORWARD;
				return result;
			} else {
				String al = request.getParameter("autologin");
				if (al != null && al.equals("1")) {
					result.forwardTo = "/Login";
					result.publishAs = PublishAsType.FORWARD;
					return result;
				}
			}
		}

		result.publishAs = PublishAsType.HTML;
		Content content = new Content(env, staticContentRule);
		result.output.append(content.getAsXML(userSession.currentUser, userSession.lang));
		SourceSupplier titleSupplier = new SourceSupplier(userSession.currentUser, env, userSession.lang);
		result.title = titleSupplier.getValueAsString(rule.getTitle().source, rule.getTitle().value)[0];
		result.addHistory = rule.addToHistory();
		return result;
	}

	@Deprecated
	private ProviderResult outline(HttpServletRequest request, IRule rule, UserSession userSession, String id) throws UnsupportedEncodingException,
	        RuleException, QueryFormulaParserException, DocumentException {
		OutlineRule outlineRule = (OutlineRule) rule;
		ProviderResult result = new ProviderResult(outlineRule.publishAs, outlineRule.getXSLT());
		Outline outline = null;

		int page = userSession.getCurrentPage(id);
		String command = request.getParameter("command");
		if (command == null) {
			command = "";
		}

		String subId = request.getParameter("subid");
		String subType = request.getParameter("subtype");

		if (subType.equals("search")) {
			String keyWord = request.getParameter("keyword");
			keyWord = new String(keyWord.getBytes("ISO-8859-1"), "UTF-8");
			result.xslt = "outline.xsl";
			outline = new SearchOutline(env, outlineRule, keyWord, page, userSession);
		} else if (subType.equals("edit")) {
			// outline = new DocumentOutline(env, id, key, page, userSession,
			// type).getOutlineAsXML((String) jses.getAttribute("lang"));
		} else if (subType.equals("filter")) {
			outline = new Outline(env, outlineRule, subType, subId, page, command, userSession);
			IRule subRule = env.ruleProvider.getRule("filter", subType);
			SourceSupplier titleSupplier = new SourceSupplier(userSession.currentUser, env, userSession.lang);
			result.title = titleSupplier.getValueAsString(subRule.getTitle().source, subRule.getTitle().value)[0];
		} else {
			outline = new Outline(env, outlineRule, subType, subId, page, command, userSession);
			IRule subRule = env.ruleProvider.getRule(subType, subId);
			SourceSupplier titleSupplier = new SourceSupplier(userSession.currentUser, env, userSession.lang);
			result.title = titleSupplier.getValueAsString(subRule.getTitle().source, subRule.getTitle().value)[0];

		}
		result.output.append(outline.getOutlineAsXML(userSession.lang));
		result.addHistory = rule.addToHistory();
		return result;
	}

	@Deprecated
	private ProviderResult view(HttpServletRequest request, HttpServletResponse response, IRule rule, UserSession userSession, String id)
	        throws ViewException, DocumentException, DocumentAccessException, RuleException, QueryFormulaParserException, QueryException,
	        LocalizatorException, ComplexObjectException {
		ViewRule viewRule = (ViewRule) rule;
		ProviderResult result = new ProviderResult(viewRule.publishAs, viewRule.getXSLT());
		View view = new View(env, viewRule, userSession, userSession.lang);
		result.publishAs = PublishAsType.HTML;
		int page = 0;
		result.disableClientCache = true;

		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}
		int parentDocProp[] = getParentDocProp(request);
		String commandURL = request.getParameter("command");

		if (commandURL != null && !commandURL.equals("null")) {
			StringTokenizer t = new StringTokenizer(commandURL, ":");
			ArrayList<String> commands = new ArrayList<String>();
			while (t.hasMoreTokens()) {
				commands.add(t.nextToken());
			}

			for (String command : commands) {
				try {
					StringTokenizer commandDetails = new StringTokenizer(command, "`");
					String commandType = commandDetails.nextToken();

					if (commandType.equals("expand")) {
						String docIDOrCat = commandDetails.nextToken();
						try {
							String docType = commandDetails.nextToken();
							DocID commandDocID = new DocID(docIDOrCat, docType);
							userSession.addExpandedThread(commandDocID);
						} catch (Exception e) {
							docIDOrCat = new String(docIDOrCat.getBytes("ISO-8859-1"), "UTF-8");
							userSession.addExpandedCategory(docIDOrCat);
						}
					} else if (commandType.equals("collaps")) {
						String docIDOrCat = commandDetails.nextToken();
						try {
							String docType = commandDetails.nextToken();
							DocID commandDocID = new DocID(docIDOrCat, docType);
							userSession.resetExpandedThread(commandDocID);
						} catch (Exception e) {
							docIDOrCat = new String(docIDOrCat.getBytes("ISO-8859-1"), "UTF-8");
							userSession.resetExpandedCategory(docIDOrCat);
						}

					}
				} catch (Exception e) {
					new PortalException("Command has not recognized, incorrect syntax of the command=" + command, env, response,
					        ProviderExceptionType.PROVIDERERROR, PublishAsType.HTML, userSession.skin);
					// return;
				}
			}
		}
		HashMap<String, String[]> fields = new HashMap<String, String[]>();
		Map<String, String[]> parMap = request.getParameterMap();
		fields.putAll(parMap);
		int pageSize = userSession.pageSize;
		if (parMap.containsKey("pagesize")) {
			try {
				pageSize = Integer.valueOf(parMap.get("pagesize")[0]);
			} catch (NumberFormatException nfe) {
				pageSize = userSession.pageSize;
			}
		}
		result.output.append(view.getContent(fields, page, pageSize, parentDocProp[0], parentDocProp[1], userSession.expandedThread,
		        userSession.expandedCategory, userSession.getFlashDoc()));
		userSession.setCurrentPage(id, page);
		SourceSupplier titleSupplier = new SourceSupplier(userSession.currentUser, env, userSession.lang);
		result.title = titleSupplier.getValueAsString(viewRule.getTitle().source, viewRule.getTitle().value)[0];
		result.addHistory = rule.addToHistory();
		return result;
	}

	private ProviderResult page(HttpServletResponse response, HttpServletRequest request, IRule rule, UserSession userSession)
	        throws DocumentException, FTIndexEngineException, RuleException, QueryFormulaParserException, UnsupportedEncodingException,
	        DocumentAccessException, QueryException, ClassNotFoundException {
		PageRule pageRule = (PageRule) rule;
		ProviderResult result = new ProviderResult(pageRule.publishAs, pageRule.getXSLT());
		result.addHistory = pageRule.addToHistory;
		HashMap<String, String[]> fields = new HashMap<String, String[]>();
		Map<String, String[]> parMap = request.getParameterMap();
		fields.putAll(parMap);
		Page page = new Page(env, userSession, pageRule);
		page.setFields(fields);

		String method = request.getMethod();
		if (method.equalsIgnoreCase("POST")) {
			page.postProcess(fields, method);
			result.publishAs = PublishAsType.JSON;
			result.jsonOutput = page.outcome;
			result.httpStatus = page.status;
		} else {
			result.output.append(page.process(fields, method));
			result.httpStatus = page.status;
		}

		if (page.fileGenerated) {
			result.publishAs = PublishAsType.OUTPUTSTREAM;
			result.filePath = page.generatedFilePath;
			result.originalAttachName = page.generatedFileOriginalName;
		}
		return result;
	}

	@Deprecated
	private ProviderResult query(HttpServletRequest request, IRule rule, UserSession userSession) throws DocumentException, FTIndexEngineException,
	        RuleException, QueryFormulaParserException, UnsupportedEncodingException, DocumentAccessException, QueryException {
		ProviderResult result = new ProviderResult();
		result.addHistory = false;
		int parentDocProp[] = getParentDocProp(request);
		HashMap<String, String[]> fields = new HashMap<String, String[]>();
		Map<String, String[]> parMap = request.getParameterMap();
		fields.putAll(parMap);
		QueryRule queryRule = (QueryRule) rule;
		Query query = QueryFactory.getQuery(env, queryRule, userSession.currentUser);
		query.setQiuckFilter(fields, env);
		int pageNum = 1;
		if (parMap.containsKey("page")) {
			try {
				pageNum = Integer.valueOf(parMap.get("page")[0]);
			} catch (NumberFormatException nfe) {
				pageNum = 1;
			}
		}
		int pageSize = userSession.pageSize;
		if (parMap.containsKey("pagesize")) {
			try {
				pageSize = Integer.valueOf(parMap.get("pagesize")[0]);
			} catch (NumberFormatException nfe) {
				pageSize = userSession.pageSize;
			}
		}
		query.fetch(pageNum, pageSize, parentDocProp[0], parentDocProp[0], new TreeSet<DocID>(), null, null, fields);
		result.output.append(query.toXML());
		return result;
	}

	private ProviderResult count(HttpServletRequest request, IRule rule, UserSession userSession) throws DocumentException, FTIndexEngineException,
	        RuleException, QueryFormulaParserException, UnsupportedEncodingException, DocumentAccessException, QueryException {
		ProviderResult result = new ProviderResult();
		HashMap<String, String[]> fields = new HashMap<String, String[]>();
		Map<String, String[]> parMap = request.getParameterMap();
		fields.putAll(parMap);

		QueryRule queryRule = (QueryRule) rule;
		Query query = QueryFactory.getQuery(env, queryRule, userSession.currentUser);

		result.output.append(query.count(fields));
		return result;
	}

	private ProviderResult search(HttpServletRequest request, UserSession userSession) throws DocumentException, FTIndexEngineException,
	        RuleException, QueryFormulaParserException, UnsupportedEncodingException, ComplexObjectException {
		ProviderResult result = new ProviderResult(PublishAsType.HTML, "searchres.xsl");
		int page = 0;
		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}
		String keyWord = request.getParameter("keyword");
		keyWord = new String(keyWord.getBytes("ISO-8859-1"), "UTF-8");
		FTSearchRequest ftRequest = new FTSearchRequest(env, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID(),
		        keyWord, page, userSession.pageSize);
		result.output.append(ftRequest.getDataAsXML());
		result.addHistory = true;
		return result;
	}

	private ProviderResult edit(HttpServletRequest request, IRule rule, UserSession userSession, String key) throws RuleException, DocumentException,
	        DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException, ClassNotFoundException, _Exception,
	        ComplexObjectException {
		String element = request.getParameter("element");
		FormRule formRule = (FormRule) rule;
		ProviderResult result = new ProviderResult(formRule.publishAs, formRule.getXSLT());
		int page = 0;
		IDatabase db = env.getDataBase();
		BaseDocument doc = null;

		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}

		Form form = null;
		if (formRule.qoEnable) {
			HashMap<String, String[]> fields = new HashMap<String, String[]>();
			HashMap<String, String[]> parMap = (HashMap<String, String[]>) request.getParameterMap();
			// Map<String, String[]> parMap =
			// ServletUtil.showParametersMap(request);
			fields.putAll(parMap);
			form = new DocumentForm(fields, env, formRule, userSession);
			// result.title =
		} else {
			form = new Form(env, formRule, userSession);
			SourceSupplier titleSupplier = new SourceSupplier(doc, userSession.currentUser, env, userSession.lang);
			result.title = titleSupplier.getValueAsString(formRule.getTitle().source, formRule.getTitle().value)[0];
		}
		int parentDocProp[] = getParentDocProp(request);

		if (key == "") {
			result.output.append(form.getDefaultFieldsAsXML(parentDocProp[0], parentDocProp[1], page, userSession.lang) + "<formsesid>"
			        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
		} else {
			if (element.equalsIgnoreCase("document")) {
				String docID = request.getParameter("docid");
				if (docID != null) {
					if (docID.equalsIgnoreCase("")) {
						result.output.append(form.getDefaultFieldsAsXML(parentDocProp[0], parentDocProp[1], page, userSession.lang) + "<formsesid>"
						        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
						result.addHistory = rule.addToHistory();
						return result;
					} else {
						doc = db.getDocumentByDdbID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
					}
				} else {
					doc = db.getMainDocumentByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
					        userSession.currentUser.getUserID());
				}
			} else if (element.equalsIgnoreCase("project")) {
				doc = db.getProjects().getProjectByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else if (element.equalsIgnoreCase("task")) {
				doc = db.getTasks().getTaskByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else if (element.equalsIgnoreCase("execution")) {
				doc = db.getExecutions().getExecutionByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else if (element.equalsIgnoreCase("discussion")) {
				doc = db.getForum().getTopicByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else if (element.equalsIgnoreCase("post")) {
				doc = db.getForum().getPostByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else if (element.equalsIgnoreCase("glossary")) {
				IGlossaries glos = db.getGlossaries();
				doc = glos.getGlossaryDocumentByID(Integer.parseInt(key), false, userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else if (element.equalsIgnoreCase("userprofile")) {
				doc = userSession.getAsDocument(env.getDataBase());
			} else if (element.equalsIgnoreCase("appprofile")) {
				doc = env.application.getAsDocument();
			}
			if (doc.isValid) {
				result.output.append(form.getFormAsXML(doc, page, doc.parentDocID, doc.parentDocType, userSession.lang) + "<formsesid>"
				        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, userSession.currentUser.getUserID());
			}

		}
		result.addHistory = rule.addToHistory();
		return result;
	}

	/**
	 * @throws LocalizatorException
	 * @throws ClassNotFoundException
	 * @throws _Exception
	 * @throws ComplexObjectException
	 * @throws NumberFormatException
	 * @deprecated
	 **/
	@Deprecated
	private ProviderResult document(HttpServletRequest request, HttpServletResponse response, IRule rule, UserSession userSession, String key,
	        String id, String type) throws DocumentAccessException, RuleException, QueryFormulaParserException, DocumentException, QueryException,
	        LocalizatorException, ClassNotFoundException, _Exception, NumberFormatException, ComplexObjectException {
		// FormRule formRule = (FormRule) rule;
		FormRule formRule = (FormRule) env.ruleProvider.getRule(FORM_RULE, id);
		ProviderResult result = new ProviderResult(formRule.publishAs, formRule.getXSLT());
		int page = 0;
		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}
		Form form = new Form(env, formRule, userSession);
		int parentDocProp[] = getParentDocProp(request);
		if (key == "") {
			result.output.append(form.getDefaultFieldsAsXML(parentDocProp[0], parentDocProp[1], page, userSession.lang) + "<formsesid>"
			        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
		} else {
			IDatabase db = env.getDataBase();
			BaseDocument doc = null;
			if (type.equals("glossary")) {
				IGlossaries glos = db.getGlossaries();
				doc = glos.getGlossaryDocumentByID(Integer.parseInt(key), false, userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
			} else {
				if (id.equalsIgnoreCase("kr") || id.equalsIgnoreCase("comment")) {
					doc = db.getTasks().getTaskByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
					        userSession.currentUser.getUserID());
				} else if (id.equalsIgnoreCase("ki")) {
					doc = db.getExecutions().getExecutionByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
					        userSession.currentUser.getUserID());
				} else if (id.equalsIgnoreCase("project") || id.equals("outdocprj") || id.equalsIgnoreCase("workdocprj")
				        || id.equalsIgnoreCase("remark")) {
					doc = db.getProjects().getProjectByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
					        userSession.currentUser.getUserID());
				} else if (id.equalsIgnoreCase("userprofile")) {
					doc = userSession.getAsDocument(env.getDataBase());
				} else if (id.equalsIgnoreCase("discussion")) {
					doc = db.getForum().getTopicByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
					        userSession.currentUser.getUserID());
				} else {
					doc = db.getMainDocumentByID(Integer.parseInt(key), userSession.currentUser.getAllUserGroups(),
					        userSession.currentUser.getUserID());
				}
			}

			if (doc.isValid) {
				result.output.append(form.getFormAsXML(doc, page, doc.parentDocID, doc.parentDocType, userSession.lang) + "<formsesid>"
				        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, userSession.currentUser.getUserID());
			}
			// SourceSupplier titleSupplier = new
			// SourceSupplier(doc,userSession.currentUser, env,
			// userSession.lang);
			result.title = "type=document, Запрос документ устарел";
		}
		result.addHistory = formRule.addToHistory();
		return result;
	}

	private ProviderResult structure(HttpServletRequest request, HttpServletResponse response, IRule rule, UserSession userSession, String key,
	        String id) throws DocumentAccessException, DocumentException, RuleException, QueryFormulaParserException, QueryException,
	        LocalizatorException, ClassNotFoundException, _Exception, ComplexObjectException {
		// FormRule formRule = (FormRule)rule;
		FormRule formRule = (FormRule) env.ruleProvider.getRule(FORM_RULE, id);
		ProviderResult result = new ProviderResult(formRule.publishAs, formRule.getXSLT());
		int page = 0;

		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}

		Form form = null;
		if (formRule.qoEnable) {
			HashMap<String, String[]> fields = new HashMap<String, String[]>();
			HashMap<String, String[]> parMap = (HashMap<String, String[]>) request.getParameterMap();
			// Map<String, String[]> parMap =
			// ServletUtil.showParametersMap(request);
			fields.putAll(parMap);
			form = new DocumentForm(fields, env, formRule, userSession);
		} else {
			form = new Form(env, formRule, userSession);
		}

		if (key == "") {
			int parentDocProp[] = getParentDocProp(request);
			result.output.append(form.getDefaultFieldsAsXML(parentDocProp[0], parentDocProp[1], page, userSession.lang) + "<formsesid>"
			        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
		} else {
			IDatabase db = env.getDataBase();
			BaseDocument doc = null;
			int docKey = Integer.parseInt(key);
			if (id.equalsIgnoreCase("organization")) {
				doc = db.getStructure().getOrganization(docKey, userSession.currentUser);
			} else if (id.equalsIgnoreCase("department")) {
				doc = db.getStructure().getDepartment(docKey, userSession.currentUser);
			} else if (id.equalsIgnoreCase("employer") || id.equalsIgnoreCase("responsibleperson") || id.equalsIgnoreCase("legalentity")
			        || id.equalsIgnoreCase("individualperson")) {
				doc = db.getStructure().getEmployer(docKey, userSession.currentUser);
			} else if (id.equalsIgnoreCase("role")) {
				// doc = db.getStructure().getRole(docKey,
				// userSession.currentUser.getAllUserGroups(),
				// userSession.currentUser.getUserID());
			} else if (id.equalsIgnoreCase("group")) {
				doc = db.getStructure().getGroup(docKey, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
			}

			if (doc != null && doc.isValid) {
				result.output.append(form.getFormAsXML(doc, page, doc.parentDocID, doc.parentDocType, userSession.lang) + "<formsesid>"
				        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, userSession.currentUser.getUserID());
			}
		}
		result.addHistory = formRule.addToHistory();
		return result;
	}

	@Deprecated
	private ProviderResult forum(HttpServletRequest request, HttpServletResponse response, IRule rule, UserSession userSession, String key, String id)
	        throws DocumentAccessException, DocumentException, RuleException, QueryFormulaParserException, QueryException, LocalizatorException,
	        ClassNotFoundException, _Exception, ComplexObjectException {
		FormRule formRule = (FormRule) rule;
		// FormRule formRule = (FormRule)env.ruleProvider.getRule(FORM_RULE,
		// id);
		ProviderResult result = new ProviderResult(formRule.publishAs, formRule.getXSLT());
		int page = 0;

		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}

		Form form = new Form(env, formRule, userSession);
		try {
			int docKey = Integer.parseInt(key);
			IDatabase db = env.getDataBase();
			BaseDocument doc = null;

			if (id.equalsIgnoreCase("topic")) {
				doc = db.getForum().getTopicByID(docKey, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
			} else if (id.equalsIgnoreCase("comment")) {
				doc = db.getForum().getPostByID(docKey, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
			}

			if (doc != null && doc.isValid) {
				result.output.append(form.getFormAsXML(doc, page, doc.parentDocID, doc.parentDocType, userSession.lang) + "<formsesid>"
				        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, userSession.currentUser.getUserID());
			}
		} catch (NumberFormatException e) {
			int parentDocProp[] = getParentDocProp(request);
			result.output.append(form.getDefaultFieldsAsXML(parentDocProp[0], parentDocProp[1], page, userSession.lang) + "<formsesid>"
			        + Util.generateRandomAsText("0qwertyuiopasdfghjklzxcvbnm123456789") + "</formsesid>");
		}

		result.addHistory = formRule.addToHistory();
		return result;
	}

	private ProviderResult save(HttpServletRequest request, HttpServletResponse response, IRule rule, UserSession userSession, String key)
	        throws UnsupportedEncodingException, WebFormValueException, LicenseException, DocumentException, RuleException,
	        QueryFormulaParserException, DocumentAccessException, ClassNotFoundException, ComplexObjectException {
		ProviderResult result = new ProviderResult();

		String element = request.getParameter("element");
		HashMap<String, String[]> fields = new HashMap<String, String[]>();
		HashMap<String, String[]> parMap = (HashMap<String, String[]>) request.getParameterMap();
		// Map<String, String[]> parMap =
		// ServletUtil.showParametersMap(request);
		fields.putAll(parMap);

		if (element != null && element.equalsIgnoreCase("user_profile")) {
			XMLResponse xmlResult = new XMLResponse(ResponseType.SAVE_FORM);
			Employer emp = userSession.currentUser.getAppUser();
			HashSet<Filter> currentFilters = new HashSet<Filter>(emp.getFilters());
			userSession.history.remove(currentFilters);
			userSession.setLang(fields.get("lang")[0], response);
			userSession.setPageSize(fields.get("pagesize")[0], response);
			userSession.flush();
			emp.getUser().fillFieldsToSaveLight(fields);
			emp.setFilters(FilterParser.parse(fields, env));
			int docID = emp.save(userSession.currentUser);

			HashSet<Filter> updatedFilters = new HashSet<Filter>(emp.getFilters());
			currentFilters.removeAll(updatedFilters);

			if (docID > -1) {
				xmlResult.setResponseStatus(true);
				xmlResult.addSignal(SignalType.RELOAD_PAGE);
			} else {
				xmlResult.setResponseStatus(false);
				xmlResult.setMessage("User has not saved");
			}

			result.output.append(xmlResult.toXML());
		} else {
			FormRule formRule = (FormRule) rule;
			if (formRule.advancedQSEnable) {
				fields = new HashMap<String, String[]>();
				parMap = (HashMap<String, String[]>) request.getParameterMap();
				fields.putAll(parMap);
				DocumentForm form = new DocumentForm(fields, env, formRule, userSession);
				int parentDocProp[] = getParentDocProp(request);
				result.output.append(form.save(key, fields, parentDocProp[0], parentDocProp[1], userSession.pageSize, userSession.lang).toXML());
			} else {
				Form form = new Form(env, formRule, userSession);
				int parentDocProp[] = getParentDocProp(request);
				result.output.append(form.save(key, fields, parentDocProp[0], parentDocProp[1], userSession.pageSize, userSession.lang).toXML());
			}
		}
		return result;
	}

	private ProviderResult getAttach(HttpServletRequest request, UserSession userSession, String key) throws UnsupportedEncodingException {
		ProviderResult result = new ProviderResult(PublishAsType.OUTPUTSTREAM, null);
		String fieldName = request.getParameter("field");
		String attachName = request.getParameter("file");

		String reqEnc = request.getCharacterEncoding();
		result.originalAttachName = new String(attachName.getBytes("ISO-8859-1"), reqEnc);

		String formSesID = request.getParameter("formsesid");
		if (formSesID != null) {
			File file = Util.getExistFile(result.originalAttachName, Environment.tmpDir + File.separator + formSesID + File.separator + fieldName
			        + File.separator);
			if (!file.exists()) {
				IDatabase dataBase = env.getDataBase();
				String docTypeAsText = request.getParameter("doctype");
				// result.filePath =
				// dataBase.getDocumentAttach(Integer.parseInt(key),
				// Integer.parseInt(docTypeAsText),
				// userSession.currentUser.getAllUserGroups(), fieldName,
				// result.originalAttachName);
				result.filePath = dataBase.getDocumentAttach(Integer.parseInt(key), Integer.parseInt(docTypeAsText), fieldName,
				        result.originalAttachName);
			} else {
				result.filePath = file.getAbsolutePath();
			}
		} else {
			IDatabase dataBase = env.getDataBase();
			String docTypeAsText = request.getParameter("doctype");
			result.filePath = dataBase.getDocumentAttach(Integer.parseInt(key), Integer.parseInt(docTypeAsText),
			        userSession.currentUser.getAllUserGroups(), fieldName, result.originalAttachName);
		}
		return result;
	}

	private ProviderResult delete(HttpServletRequest request, UserSession userSession) {
		ProviderResult result = new ProviderResult();
		String ck = request.getParameter("ck");
		List<DocID> keys = ComplexKeyParser.parse(ck);
		String completely = request.getParameter("typedel");
		boolean completeRemove;
		if (completely != null && completely.equalsIgnoreCase("1")) {
			completeRemove = true;
		} else {
			completeRemove = false;
		}
		IDatabase dataBase = env.getDataBase();
		User currentUser = userSession.currentUser;
		XMLResponse xmlResp = dataBase.deleteDocuments(keys, completeRemove, currentUser);
		result.output.append(xmlResp.toXML());
		return result;
	}

	private ProviderResult undelete(HttpServletRequest request, UserSession userSession) {
		ProviderResult result = new ProviderResult();
		String ck = request.getParameter("ck");
		List<DocID> keys = ComplexKeyParser.parse(ck);
		IDatabase dataBase = env.getDataBase();
		User currentUser = userSession.currentUser;
		XMLResponse xmlResp = dataBase.unDeleteDocuments(keys, currentUser);
		result.output.append(xmlResp.toXML());
		return result;
	}

	@Deprecated
	private ProviderResult handler(HttpServletRequest request, IRule rule, UserSession userSession) throws Exception {
		ProviderResult result = new ProviderResult();
		result.addHistory = false;
		HandlerRule handlerRule = (HandlerRule) rule;
		if (handlerRule.trigger == TriggerType.PROVIDER) {
			result.output.append("<handler>");
			User user = userSession.currentUser;
			if (handlerRule.runUnderUser.getSourceType() == ValueSourceType.STATIC) {
				user = new User(handlerRule.runUnderUser.value, env);
			}

			if (handlerRule.scriptIsValid) {
				HandlerScriptProcessor handler = new HandlerScriptProcessor(env, user, request.getParameterMap(), userSession.lang);
				handler.setClassName(handlerRule.handlerClassName);
				if (handlerRule.waitResponse) {
					XMLResponse xmlResp = handler.processScript();
					result.output.append(xmlResp.toXML());
					if (xmlResp.type == ResponseType.SHOW_FILE_AFTER_HANDLER_FINISHED) {
						result.publishAs = PublishAsType.OUTPUTSTREAM;
						result.filePath = xmlResp.getMessage("full_file_path").text;
						result.originalAttachName = xmlResp.getMessage("original_name").text;
					}
				} else {
					Thread t = new Thread(handler);
					t.start();
					XMLResponse xmlResp = new XMLResponse(ResponseType.DO_HANDLER_THREAD);
					xmlResp.setResponseStatus(true);
					result.output.append(xmlResp.toXML());
				}
			} else {
				HandlerScriptProcessor wfsp = new HandlerScriptProcessor(request.getParameterMap(), env, user);
				AppEnv.logger.verboseLogEntry("Run handler \"" + handlerRule.id + "\"");
				if (handlerRule.waitResponse) {
					String res = wfsp.process(handlerRule.getScript(), false);
					if (!((HandlerRule) rule).showFile) {
						result.output.append(res);
					} else {
						result.publishAs = PublishAsType.OUTPUTSTREAM;
						result.filePath = res;
						result.originalAttachName = new File(res).getName();
					}
					/*
					 * AppEnv.logger.verboseLogEntry(
					 * "Attempt to run handler (ruleid=" + handlerRule.id +
					 * ")"); String result =
					 * wfsp.process(handlerRule.getScript()); if
					 * (!handlerRule.showFile){ output.append(result); }else{
					 * publishAs = PublishAsType.OUTPUTSTREAM; filePath =
					 * result; originalAttachName = new File(result).getName();
					 * }
					 */

				} else {
					HandlerLauncher hl = new HandlerLauncher(wfsp, handlerRule.getScript());
					hl.start();
				}
			}
			result.output.append("</handler>");
		} else {
			AppEnv.logger.verboseLogEntry("Handler \"" + handlerRule.id + "\" is not set to launch by provider");
		}
		return result;
	}

	@Deprecated
	private ProviderResult service(HttpServletRequest request, HttpServletResponse response, UserSession userSession, String key, IRule rule)
	        throws InterruptedException, RuleException, QueryFormulaParserException, DocumentException, DocumentAccessException,
	        ComplexObjectException {
		String element = request.getParameter("element");
		ProviderResult result = new ProviderResult();
		IDatabase db = env.getDataBase();
		IUsersActivity ua = db.getUserActivity();
		int docType = DOCTYPE_UNKNOWN;
		int docID = 0;
		try {
			docID = Integer.parseInt(key);
			docType = Integer.parseInt(request.getParameter("doctype"));
		} catch (NumberFormatException nfe) {
			docType = DOCTYPE_UNKNOWN;
		}
		int page = 0;
		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (NumberFormatException nfe) {
			page = 1;
		}

		int pageSize = userSession.pageSize;
		if (request.getParameterMap().containsKey("pagesize")) {
			try {
				pageSize = Integer.valueOf(request.getParameterMap().get("pagesize")[0]);
			} catch (NumberFormatException nfe) {
				pageSize = userSession.pageSize;
			}
		}
		String operation = request.getParameter("operation");
		if (operation.equalsIgnoreCase("mark_as_read")) {
			Thread.sleep(env.globalSetting.markAsReadMsDelay);
			ua.postMarkRead(docID, docType, userSession.currentUser);
		} else if (operation.equalsIgnoreCase("mark_as_unread")) {
			ua.postMarkUnread(docID, docType, userSession.currentUser);
		} else if (operation.equalsIgnoreCase("users_which_read")) {
			result.output.append(ua.getUsersWhichRead(docID, docType, env));
		} else if (operation.equalsIgnoreCase("user_activity")) {
			String userid = request.getParameter("userid");
			result.output.append(ua.getActivity(userid, page, pageSize));
		} else if (operation.equalsIgnoreCase("service_activity")) {
			String serviceName = request.getParameter("servicename");
			result.output.append(ua.getActivityByService(serviceName, page, pageSize, UsersActivityType.CUSTOM_EVENT.getCode()));
		} else if (operation.equalsIgnoreCase("users_by_role")) {
			String role = request.getParameter("role");
			result.output.append(env.getDataBase().getStructure().getEmployersByRoles(role));
		} else if (operation.equalsIgnoreCase("add_to_favourites")) {
			ua.postAddToFavorites(docID, docType, userSession.currentUser.getUserID(), userSession.currentUser.getAllUserGroups());
		} else if (operation.equalsIgnoreCase("get_accesslist")) {
			ServiceHandler sh = new ServiceHandler();
			result.output.append(sh.getAccessList(db, request.getParameter("docid")));
		} else if (operation.equalsIgnoreCase("get_day_diff")) {
			if (docType == Const.DOCTYPE_TASK) {
				// Calendar currentDate = Calendar.getInstance();
				// Calendar ctrlDate = Calendar.getInstance();
				// ctrlDate.setTime(Util.convertStringToDate(request.getParameterMap().get("ctrldate")[0]));
				// Calendar[] holidays = null;
				// int day_diff = RuntimeObjUtil.getDiffBetweenDays(currentDate,
				// ctrlDate, holidays, false);
				int day_diff = env.getDataBase().getTasks().recalculate(docID);
				XMLResponse xmlResp = new XMLResponse(ResponseType.RESULT_OF_HANDLER_SERVICE);
				xmlResp.addMessage(Integer.toString(day_diff));
				result.output.append(xmlResp.toXML());
			} else if (docType == Const.DOCTYPE_PROJECT) {
				int day_diff = env.getDataBase().getProjects().recalculate(docID);
				XMLResponse xmlResp = new XMLResponse(ResponseType.RESULT_OF_HANDLER_SERVICE);
				xmlResp.addMessage(Integer.toString(day_diff));
				result.output.append(xmlResp.toXML());
			}
		} else if (operation.equals("tune_session")) {
			if (element.equalsIgnoreCase("page")) {
				RunTimeParameters rtp = new RunTimeParameters();
				rtp.parseParameters(request.getParameterValues("param"));
				userSession.setRuntimeConditions(RuleType.UNKNOWN + "_" + request.getParameter("id"), rtp);
			}
		} else if (operation.equalsIgnoreCase("get_ctrl_date")) {
			int priority = 0, complication = 0;
			try {
				priority = Integer.parseInt(request.getParameter("priority"));
			} catch (NumberFormatException nfe) {

			}
			try {
				complication = Integer.parseInt(request.getParameter("complication"));
			} catch (NumberFormatException nfe) {

			}

			Calendar ctrlDate = RuntimeObjUtil.getCtrlDate(Calendar.getInstance(), priority, complication);

			XMLResponse xmlResp = new XMLResponse(ResponseType.RESULT_OF_HANDLER_SERVICE);
			System.out.println("---" + Util.convertDataTimeToString(ctrlDate));
			result.output.append(xmlResp.toXML());

		} else if (operation.equalsIgnoreCase("remove_from_favourites")) {
			ua.postRemoveFromFavorites(docID, docType, userSession.currentUser.getUserID(), userSession.currentUser.getAllUserGroups());
		} else if (operation.equalsIgnoreCase("fields_to_sign")) {
			FormRule formRule = (FormRule) env.ruleProvider.getRule(FORM_RULE, key);
			result.output.append(formRule.getFieldsToSign());
		} else if (operation.equalsIgnoreCase("check_sign")) {
			// FormRule formRule = (FormRule)env.ruleProvider.getRule(FORM_RULE,
			// key);
		} else if (operation.equalsIgnoreCase("")) {
			// FormRule formRule = (FormRule)env.ruleProvider.getRule(FORM_RULE,
			// key);
		} else if (operation.equalsIgnoreCase("zhutdown")) {
			Server.shutdown();
		} else if (operation.equalsIgnoreCase("")) {

		}
		return result;
	}

	private void disableCash(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache, must-revalidate, private, no-store, s-maxage=0, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	private int[] getParentDocProp(HttpServletRequest request) {
		int[] prop = new int[2];
		try {
			prop[0] = Integer.parseInt(request.getParameter("parentdocid"));
		} catch (NumberFormatException nfe) {
			prop[0] = 0;
		}
		try {
			prop[1] = Integer.parseInt(request.getParameter("parentdoctype"));
		} catch (NumberFormatException nfe) {
			prop[1] = DOCTYPE_UNKNOWN;
		}
		return prop;
	}
}
