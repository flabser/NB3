package kz.flabs.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.PortalException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.ServerException;
import kz.flabs.exception.ServerExceptionType;
import kz.flabs.exception.TransformatorException;
import kz.flabs.exception.XSLTFileNotFoundException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.FTSearchRequest;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.servlets.sitefiles.AttachmentHandler;
import kz.flabs.servlets.sitefiles.AttachmentHandlerException;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.flabs.users.User;
import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;
import kz.flabs.util.Util;
import kz.flabs.webrule.IRule;
import kz.flabs.webrule.Skin;
import kz.flabs.webrule.page.PageRule;
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

					if (type.equalsIgnoreCase("page")) {
						result = page(response, request, rule, userSession);
						if (result.publishAs == PublishAsType.OUTPUTSTREAM) {
							attachHandler = new AttachmentHandler(request, response, true);
						}
						// return;

					} else if (type.equalsIgnoreCase("search")) {
						result = search(request, userSession);
					} else if (type.equalsIgnoreCase("getattach")) {
						result = getAttach(request, userSession, key);
						attachHandler = new AttachmentHandler(request, response, true);

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
		} catch (ServerException e) {
			new PortalException(e, response, ProviderExceptionType.SERVER, PublishAsType.HTML);
		} catch (Exception e) {
			new PortalException(e, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML);
		}

		// System.out.println("done...");

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
			result.jsonOutput = page.response.outcome;
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
				// result.filePath =
				// dataBase.getDocumentAttach(Integer.parseInt(key),
				// Integer.parseInt(docTypeAsText), fieldName,
				// result.originalAttachName);
			} else {
				result.filePath = file.getAbsolutePath();
			}
		} else {
			IDatabase dataBase = env.getDataBase();
			String docTypeAsText = request.getParameter("doctype");
			// result.filePath =
			// dataBase.getDocumentAttach(Integer.parseInt(key),
			// Integer.parseInt(docTypeAsText),
			// userSession.currentUser.getAllUserGroups(), fieldName,
			// result.originalAttachName);
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
