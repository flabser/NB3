package kz.flabs.servlets.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.IUsersActivity;
import kz.flabs.dataengine.h2.alter.Updates;
import kz.flabs.dataengine.h2.holiday.HolidayCollection;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.PortalException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.exception.XSLTFileNotFoundException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.queries.GlossaryQuery;
import kz.flabs.runtimeobj.queries.Query;
import kz.flabs.scriptprocessor.handler.GroovyScriptProcessor;
import kz.flabs.servlets.ProviderExceptionType;
import kz.flabs.servlets.ProviderResult;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.SaxonTransformator;
import kz.flabs.servlets.ServletUtil;
import kz.flabs.servlets.sitefiles.AttachmentHandler;
import kz.flabs.servlets.sitefiles.AttachmentHandlerException;
import kz.flabs.servlets.sitefiles.AttachmentHandlerExceptionType;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLResponse;
import kz.flabs.webrule.IRule;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.page.PageRule;
import kz.flabs.webrule.query.QueryRule;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ViewEntryCollection;
import kz.pchelka.backup.Backup;
import kz.pchelka.backup.BackupList;
import kz.pchelka.env.Environment;
import kz.pchelka.log.LogFiles;
import kz.pchelka.scheduler.BackgroundProcCollection;
import kz.pchelka.scheduler.IDaemon;
import kz.pchelka.scheduler.PeriodicalServices;
import kz.pchelka.scheduler.Scheduler;
import kz.pchelka.server.Server;

public class AdminProvider extends HttpServlet implements Const {
	public static final int pageSize = 30;

	private static final long serialVersionUID = 2352885167311108325L;
	private ISystemDatabase sysDb;
	private AppEnv env;
	private String defaultSkin = "pchelka";

	@Override
	public void init(ServletConfig config) throws ServletException {
		sysDb = DatabaseFactory.getSysDatabase();
		try {
			ServletContext context = config.getServletContext();
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
		AttachmentHandler attachHandler = null;
		ProviderResult result = null;

		try {
			request.setCharacterEncoding("utf-8");
			String type = request.getParameter("type");
			String element = request.getParameter("element");
			String id = request.getParameter("id");
			String key = request.getParameter("key");
			String app = request.getParameter("app");
			String dbID = request.getParameter("dbid");
			String onlyXML = request.getParameter("onlyxml");

			String disposition;
			if (request.getParameter("disposition") != null) {
				disposition = request.getParameter("disposition");
			} else {
				disposition = "attachment";
			}
			StringBuffer output = new StringBuffer(10000);
			boolean disableClientCache = false;

			System.out.println("Web request type=" + type + ", element=" + element + ", id=" + id + ", app=" + app + ", dbid=" + dbID);
			HttpSession jses = request.getSession(true);
			jses.setAttribute("lang", "EN");
			jses.setAttribute("skin", "");

			if (jses.getAttribute("adminLoggedIn") == null) {
				response.sendRedirect("/");
				return;
			}

			if (type != null) {
				if (type.equalsIgnoreCase("view")) {
					result = view(request, dbID, app, element, id);
				} else if (type.equalsIgnoreCase("edit")) {
					result = edit(request, app, element, id, key);
				} else if (type.equalsIgnoreCase("save")) {
					result = save(request, app, dbID, element, id);
				} else if (type.equalsIgnoreCase("delete")) {
					result = delete(request, app, element, id);
				} else if (type.equalsIgnoreCase("service")) {
					result = service(request, app, id, key);
				} else if (type.equalsIgnoreCase("get_form")) {
					// xslt = "forms"+File.separator+"form.xsl";
					IRule rule = Environment.getApplication(app).ruleProvider.getRule(FORM_RULE, id);
					output.append(rule.getRuleAsXML(app));

				} else if (type.equalsIgnoreCase("get_users_by_key")) {
					result = new ProviderResult();
					String keyWord = request.getParameter("keyword");
					ArrayList<User> users = sysDb.getUsers(keyWord);
					result.output.append("<users>");
					for (User user : users) {
						result.output.append("<entry>" + user.usersByKeytoXML() + "</entry>");
					}
					result.output.append("</users>");

				} else if (type.equalsIgnoreCase("delete_maindoc")) {
					IDatabase db = DatabaseFactory.getDatabaseByName(dbID);
					try {
						db.deleteDocument(DOCTYPE_MAIN, Integer.parseInt(id), new User(Const.sysUser, env), true);
						output.append(new XMLResponse(ResponseType.DELETE_DOCUMENT, true).toXML());
					} catch (Exception e) {
						output.append(new XMLResponse(e).toXML());
					}
				} else if (type.equalsIgnoreCase("delete_glossary")) {
					IDatabase db = DatabaseFactory.getDatabaseByName(dbID);
					IGlossaries glos = db.getGlossaries();
					try {
						glos.deleteGlossaryDocument(Integer.parseInt(id));
						output.append(new XMLResponse(ResponseType.DELETE_DOCUMENT, true).toXML());
					} catch (Exception e) {
						output.append(new XMLResponse(e).toXML());
					}
				} else if (type.equalsIgnoreCase("get_glossary")) { // get
					                                                // glossary
					// xslt = "views"+File.separator+"glossary.xsl";
					AppEnv env = Environment.getApplication(app);
					WebRuleProvider wrp = env.ruleProvider;
					QueryRule rule = (QueryRule) wrp.getRule(QUERY_RULE, id);
					ISystemDatabase sysDb = DatabaseFactory.getSysDatabase();
					Query query = new GlossaryQuery(env, rule, sysDb.getUser(Const.sysUser));
					output.append(query.toXML());

				} else {
					throw new PortalException("Request has not been recognized (type=" + type + ")", env, response,
					        ProviderExceptionType.PROVIDERERROR, PublishAsType.HTML, defaultSkin);
					// return;
				}
			} else {
				throw new PortalException("Request is incorrect(type=null)", env, response, ProviderExceptionType.PROVIDERERROR, PublishAsType.HTML,
				        defaultSkin);
				// return;
			}

			if (disableClientCache) {
				response.setHeader("Cache-Control", "no-cache, must-revalidate, private, no-store, s-maxage=0, max-age=0");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);
			}

			if (onlyXML != null) {
				result.publishAs = PublishAsType.XML;
			}

			AdminProviderOutput po = new AdminProviderOutput(type, element, id, result.output, request, response, new UserSession(new User(
			        Const.sysUser), request), jses, dbID);
			if (result.publishAs == PublishAsType.HTML) {
				if (result.disableClientCache) {
					// disableCash(response);
				}

				response.setContentType("text/html");

				if (po.prepareXSLT(result.xslt)) {
					String outputContent = po.getStandartOutput();
					// long start_time = System.currentTimeMillis(); // for
					// speed debuging
					new SaxonTransformator().toTrans(response, po.xslFile, outputContent);
					// System.out.println(getClass().getSimpleName() +
					// " transformation  >>> " +
					// Util.getTimeDiffInMilSec(start_time)); // for speed
					// debuging
				} else {
					String outputContent = po.getStandartOutput();
					response.setContentType("text/xml;charset=utf-8");
					PrintWriter out = response.getWriter();
					out.println(outputContent);
					out.close();
				}
			} else if (result.publishAs == PublishAsType.XML) {
				if (result.disableClientCache) {
					// disableCash(response);
				}
				response.setContentType("text/xml;charset=utf-8");

				String outputContent = po.getStandartUTF8Output();
				// System.out.println(outputContent);
				PrintWriter out = response.getWriter();
				out.println(outputContent);
				out.close();
			} else if (result.publishAs == PublishAsType.TEXT) {
				if (result.disableClientCache) {
					// disableCash(response);
				}

				String outputContent = po.getPlainText();
				response.setContentType("text/text;charset=utf-8");
				response.getWriter().println(outputContent);
			} else if (result.publishAs == PublishAsType.OUTPUTSTREAM) {
				if (request.getParameter("disposition") != null) {
					disposition = request.getParameter("disposition");
				} else {
					disposition = "attachment";
				}
				attachHandler = new AttachmentHandler(request, response, true);
				attachHandler.publish(result.filePath, result.originalAttachName, disposition);
			} else if (result.publishAs == PublishAsType.FORWARD) {
				response.sendRedirect(result.forwardTo);
				return;
			}

		} catch (XSLTFileNotFoundException xfnf) {
			new PortalException(xfnf, env, response, PublishAsType.HTML, defaultSkin);
		} catch (IOException ioe) {
			new PortalException(ioe, env, response, PublishAsType.HTML, defaultSkin);
		} catch (Exception e) {
			new PortalException(e, env, response, PublishAsType.HTML, defaultSkin);
		}
	}

	private ProviderResult view(HttpServletRequest request, String dbID, String app, String element, String id) throws RuleException,
	        DocumentException, DocumentAccessException, ComplexObjectException, _Exception {
		ProviderResult result = new ProviderResult();
		result.publishAs = PublishAsType.HTML;
		ServiceHandler sh = null;
		String content = "";
		AppEnv env = null;
		IDatabase db = null;
		if (app != null && !"".equalsIgnoreCase(app)) {
			env = Environment.getApplication(app);
			dbID = env.getDataBase().getDbID();
			db = DatabaseFactory.getDatabaseByName(dbID);
			sh = new ServiceHandler(dbID);
		} else if (dbID != null && !"".equalsIgnoreCase(dbID)) {
			db = DatabaseFactory.getDatabaseByName(dbID);
		} else {
			sh = new ServiceHandler();
		}

		int count = 0;
		int page = ServletUtil.getPage(request);

		if (element.equalsIgnoreCase("cfg")) {
			result.xslt = "forms" + File.separator + "cfg.xsl";
			content = sh.getCfg();
		} else if (element.equalsIgnoreCase("console")) {
			result.xslt = "views" + File.separator + "console.xsl";
			// result.output.append("<console sesid=\"" + jses.getId() +
			// "\" server=\"" + Environment.remoteConsoleServer + "\" port=\"" +
			// Environment.remoteConsolePort + "\"></console>");
		} else if (element.equalsIgnoreCase("logs")) {
			LogFiles logs = new LogFiles();
			result.xslt = "views" + File.separator + "logs_list.xsl";
			count = logs.getCount();
			content = sh.getLogsListWrapper(logs);
		} else if (element.equalsIgnoreCase("users")) {
			result.xslt = "views" + File.separator + "users_list.xsl";
			UserServices us = new UserServices();
			String keyWord = request.getParameter("keyword");
			content = us.getUserListWrapper(keyWord, page, pageSize);
			count = us.getCount();
		} else if (element.equalsIgnoreCase("scheduler")) {
			result.xslt = "views" + File.separator + "scheduler_list.xsl";
			BackgroundProcCollection pc = Environment.scheduler.getProcesses();
			if (pc != null) {
				count = pc.size();
				content = pc.getProcessAsXMLPiece();
			}
		} else if (element.equalsIgnoreCase("backup")) {
			BackupList b = new BackupList();
			result.xslt = "views" + File.separator + "backup_list.xsl";
			count = b.getCount();
			content = sh.getBackupListWrapper(b.getFileList(page, pageSize));
		} else if (element.equalsIgnoreCase("calendar")) {
			result.xslt = "views" + File.separator + "calendar_list.xsl";
			HolidayCollection holidays = Environment.systemBase.getHolidayCol(Calendar.getInstance().get(Calendar.YEAR), page, pageSize);
			count = holidays.holidays.size();
			content = sh.getCalendarListWrapper(holidays, page, pageSize);
		} else if (element.equalsIgnoreCase("activity")) {
			result.xslt = "views" + File.separator + "activity.xsl";
			IUsersActivity ua = db.getUserActivity();
			count = ua.getAllActivityCount();
			content = ua.getAllActivity(db.calcStartEntry(page, pageSize), pageSize).toString();
		} else if (element.equalsIgnoreCase("document_activity")) {
			result.xslt = "views" + File.separator + "activity.xsl";
			String docID = request.getParameter("docid");
			content = db.getUserActivity().getActivity(docID).toString();
		} else if (element.equalsIgnoreCase("pages")) {
			result.xslt = "views" + File.separator + "pages_list.xsl";
			RuleServices rs = new RuleServices();
			content = rs.getPageRuleList(page, app, false);

		} else if (element.equalsIgnoreCase("handlers")) {
			result.xslt = "views" + File.separator + "handler_list.xsl";
			RuleServices rs = new RuleServices();
			content = rs.getHandlerRuleList(page, app, false);
		} else if (element.equalsIgnoreCase("settings")) {
			result.xslt = "forms" + File.separator + "settings.xsl";
			content = sh.getSettings(env);
		} else if (element.equalsIgnoreCase("documents")) {
			result.xslt = "views" + File.separator + "maindoc_list.xsl";
			DatabaseServices ds = new DatabaseServices(dbID);
			_ViewEntryCollection col = ds.getAllDocuments(env, page, pageSize);
			count = col.getCount();
			content = ds.wrapDocumentsList(col);
		} else if (element.equalsIgnoreCase("glossaries")) {
			result.xslt = "views" + File.separator + "glossary_list.xsl";
			DatabaseServices ds = new DatabaseServices(dbID);
			content = ds.getAllDocsAsXML("glossary", page, app);
		}

		result.output.append("<query count=\"" + count + "\" currentpage=\"" + page + "\" maxpage=\"" + RuntimeObjUtil.countMaxPage(count, pageSize)
		        + "\">" + content + "</query>");
		return result;
	}

	private ProviderResult edit(HttpServletRequest request, String app, String element, String id, String key) throws NumberFormatException,
	        RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException,
	        ComplexObjectException {
		ProviderResult result = new ProviderResult();
		result.publishAs = PublishAsType.HTML;
		ServiceHandler sh = null;
		AppEnv appEnv = null;
		String dbID = "";
		if (app != null) {
			appEnv = Environment.getApplication(app);
			dbID = Environment.getApplication(app).getDataBase().getDbID();
			sh = new ServiceHandler(dbID);
		} else {
			sh = new ServiceHandler();
		}

		result.output.append("<document>");
		if (element.equalsIgnoreCase("cfg")) {
			result.xslt = "forms" + File.separator + "cfg.xsl";
			result.output.append(sh.getCfg());
		} else if (element.equalsIgnoreCase("log")) {
			LogFiles logs = new LogFiles();
			// result.attachHandler = new AttachmentHandler(request, response,
			// true);
			result.filePath = logs.logDir + File.separator + id;
			result.originalAttachName = id;
			result.publishAs = PublishAsType.OUTPUTSTREAM;
		} else if (element.equalsIgnoreCase("user")) {
			result.xslt = "forms" + File.separator + "user.xsl";
			UserServices us = new UserServices();
			if (key == null || key.equals("")) {
				result.output.append(us.getBlankUserAsXML());
			} else {
				result.output.append(us.getUserAsXML(Integer.parseInt(key)));
			}
		} else if (element.equalsIgnoreCase("schedule")) {
			result.xslt = "forms" + File.separator + "schedule.xsl";
			BackgroundProcCollection procCollection = Environment.scheduler.getProcesses();
			IAdministartorForm daemon = (IAdministartorForm) procCollection.getProcess(id);
			result.output.append(daemon.toXML());
		} else if (element.equalsIgnoreCase("backup")) {
			result.xslt = "forms" + File.separator + "backup.xsl";
			BackupList backupList = new BackupList();
			Backup b = new Backup(backupList, id, app);
			result.output.append(b.toXML());
		} else if (element.equalsIgnoreCase("handler_rule")) {
			result.xslt = "forms" + File.separator + "handler.xsl";
			HandlerRule rule = (HandlerRule) Environment.getApplication(app).ruleProvider.getRule(HANDLER_RULE, id);
			result.output.append(rule.getRuleAsXML(app));
		} else if (element.equalsIgnoreCase("page_rule")) {
			result.xslt = "forms" + File.separator + "page.xsl";
			PageRule rule = (PageRule) appEnv.ruleProvider.getRule(PAGE_RULE, id);
			result.output.append(rule.getRuleAsXML(app));
		} else if (element.equalsIgnoreCase("document")) {
			result.xslt = "forms" + File.separator + "document.xsl";
			result.output.append(sh.getMainDoc(request.getParameter("dbid"), id));
		} else if (element.equalsIgnoreCase("glossary")) {
			result.xslt = "forms" + File.separator + "document.xsl";
			result.output.append(sh.getMainDoc(request.getParameter("dbid"), id));
		}
		result.output.append("</document>");
		return result;

	}

	private ProviderResult save(HttpServletRequest request, String app, String dbID, String element, String id) throws WebFormValueException,
	        RuleException, QueryFormulaParserException, DocumentException, DocumentAccessException, ComplexObjectException, LicenseException {
		ProviderResult result = new ProviderResult();
		XMLResponse xmlResp = new XMLResponse(ResponseType.SAVE_FORM, true);

		if (element.equalsIgnoreCase("document")) {
			// Map<String, String[]> fields =
			// ServletUtil.showParametersMap(request);
			Map<String, String[]> fields = request.getParameterMap();
			IDatabase db = DatabaseFactory.getDatabaseByName(dbID);
			String docID = request.getParameter("docid");
			BaseDocument baseDoc = db.getDocumentByDdbID(docID, Const.supervisorGroupAsSet, Const.sysUser);
			Document doc = (Document) baseDoc;

			doc.clearReaders();
			for (String r : fields.get("reader")) {
				doc.addReader(r);
			}

			doc.clearEditors();
			for (String e : fields.get("editor")) {
				doc.addEditor(e);
			}

			doc.save(new User(Const.sysUser));
		} else if (element.equalsIgnoreCase("user_profile")) {
			UserServices us = new UserServices();
			result.output
			        .append(new XMLResponse(ResponseType.SAVE_FORM_OF_USER_PROFILE, us.saveUser(ServletUtil.showParametersMap(request))).toXML());
		} else if (element.equalsIgnoreCase("handler_rule")) {
			WebRuleProvider wrp = Environment.getApplication(app).ruleProvider;
			HandlerRule rule = (HandlerRule) wrp.getRule(HANDLER_RULE, id);
			if (rule != null) {
				@SuppressWarnings("unchecked")
				Map<String, String[]> parMap = ServletUtil.showParametersMap(request);
				rule.setScript(parMap.get("script")[0].replace("&lt;", "<").replace("&gt;", ">"));
				rule.setDescription(parMap.get("description")[0]);
			}
		} else {
			xmlResp.resultFlag = false;
		}
		result.output.append(xmlResp.toXML());
		return result;
	}

	private ProviderResult delete(HttpServletRequest request, String app, String element, String id) throws WebFormValueException, RuleException,
	        QueryFormulaParserException {
		ProviderResult result = new ProviderResult();

		String dbID = "";
		if (app != null) {
			dbID = Environment.getApplication(app).getDataBase().getDbID();
		}

		result.output.append("<delete>");
		if (element.equalsIgnoreCase("user")) {
			UserServices us = new UserServices();
			result.output.append(new XMLResponse(ResponseType.DELETE_USER, us.deleteUser(id)).toXML());
		} else if (element.equalsIgnoreCase("log")) {
			LogFiles logs = new LogFiles();
			String filePath = logs.logDir + File.separator + id;
			File file = new File(filePath);
			file.delete();
		} else if (element.equalsIgnoreCase("document")) {
			int docID = Integer.parseInt(request.getParameter("docid"));
			int docType = Integer.parseInt(request.getParameter("doctype"));
			IDatabase db = DatabaseFactory.getDatabaseByName(dbID);
			XMLResponse xmlResp;
			try {
				db.deleteDocument(docType, docID, new User(Const.sysUser, env), true);
				xmlResp = new XMLResponse(ResponseType.DELETE_DOCUMENT, true);
			} catch (Exception e) {
				xmlResp = new XMLResponse(ResponseType.DELETE_DOCUMENT, false);
			}
			result.output.append(xmlResp.toXML());
		}

		result.output.append("</delete>");
		return result;
	}

	private ProviderResult service(HttpServletRequest request, String app, String id, String key) throws RuleException, QueryFormulaParserException,
	        ClassNotFoundException, AttachmentHandlerException {
		ProviderResult result = new ProviderResult();
		String operation = request.getParameter("operation");

		if (operation.equalsIgnoreCase("post_reg_num")) {
			String value = request.getParameter("value");
			Environment.getApplication(app).getDataBase().postRegNum(Integer.parseInt(value), key);
		} else if (operation.equalsIgnoreCase("reset_all_rules")) {
			WebRuleProvider ruleProvider = Environment.getApplication(app).ruleProvider;
			result.output.append(new XMLResponse(ResponseType.RESET_RULES, ruleProvider.resetRules()).toXML());
		} else if (operation.equalsIgnoreCase("reset_xslt")) {
			Environment.getApplication(app).xsltFileMap.clear();
			result.output.append(new XMLResponse(ResponseType.RESET_XSLT).toXML());
		} else if (operation.equalsIgnoreCase("do_saved_handler") || operation.equalsIgnoreCase("do_handler")) {
			AppEnv env = Environment.getApplication(app);
			User user = new User(sysUser, env);
			GroovyScriptProcessor handler = new GroovyScriptProcessor(env, user, request.getParameterMap());
			WebRuleProvider wrp = Environment.getApplication(app).ruleProvider;
			HandlerRule handlerRule = (HandlerRule) wrp.getRule(HANDLER_RULE, id);
			if (handlerRule.getTriggerType() == TriggerType.MANUALLY) {
				handler.setClassName(handlerRule.handlerClassName);
				if (handlerRule.waitResponse) {
					XMLResponse xmlResp = handler.runScript();
					result.output.append(xmlResp.toXML());
					if (xmlResp.type == ResponseType.SHOW_FILE_AFTER_HANDLER_FINISHED) {
						result.filePath = xmlResp.getMessage("full_file_path").text;
						File file = new File(result.filePath);
						if (file.exists()) {
							result.originalAttachName = file.getName();
							// attachHandler = new AttachmentHandler(request,
							// response, false);
						} else {
							throw new AttachmentHandlerException(AttachmentHandlerExceptionType.FILE_NOT_FOUND, result.filePath);
						}
					}
				} else {
					Thread t = new Thread(handler);
					t.start();
					XMLResponse xmlResp = new XMLResponse(ResponseType.DO_HANDLER_THREAD);
					xmlResp.setResponseStatus(true);
					result.output.append(xmlResp.toXML());
				}
			} else if (handlerRule.getTriggerType() == TriggerType.SCHEDULER) {
				XMLResponse xmlResp = new XMLResponse(ResponseType.DO_SCHEDULED_HANDLER);
				try {
					if (handlerRule.scriptIsValid()) {
						Class c = Class.forName(handlerRule.getClassName());
						IDaemon daemon = (IDaemon) c.newInstance();
						daemon.init(handlerRule);
						daemon.process(handlerRule.getOwner());
						xmlResp.setResponseStatus(true);
					}
				} catch (Exception e) {
					xmlResp.setResponseStatus(false);
					xmlResp.setMessage(e.toString());
					AppEnv.logger.errorLogEntry(e);
				}

				result.output.append(xmlResp.toXML());
			} else if (handlerRule.getTriggerType() == TriggerType.PROVIDER) {
				XMLResponse xmlResp = new XMLResponse(ResponseType.DO_PROVIDER_HANDLER);
				xmlResp.setResponseStatus(false);
				xmlResp.setMessage("Provider handler deprecated. You should use Page element");
				result.output.append(xmlResp.toXML());
			}

		} else if (operation.equalsIgnoreCase("do_scheduled_handler")) {
			XMLResponse xmlResp = new XMLResponse(ResponseType.DO_SCHEDULED_HANDLER);
			Scheduler sched = Environment.scheduler;
			PeriodicalServices periodicalServices = sched.periodicalServices;
			if (periodicalServices != null) {
				ArrayList<IDaemon> activProceses = periodicalServices.getCurrentTasks();
				for (IDaemon daemon : activProceses) {
					if (daemon.getID().equals(id)) {
						Server.logger.normalLogEntry("Launch(unscheduled)>" + daemon.getID());
						Thread t = new Thread(daemon);
						t.setName("Scheduler task:" + daemon.getID());
						t.start();
						break;
					}
				}
				xmlResp.setResponseStatus(true);
			} else {
				xmlResp.setResponseStatus(false);
				xmlResp.setMessage("Scheduler has not been started. Try later");
			}

			result.output.append(xmlResp.toXML());
		} else if (operation.equalsIgnoreCase("run_database_patch")) {
			XMLResponse resp = null;
			IDatabase db = Environment.getApplication(app).getDataBase();
			String dbID = db.getDbID();
			Connection conn = db.getConnectionPool().getConnection();
			Integer patchID = Integer.parseInt(id);
			try {
				resp = new XMLResponse(ResponseType.RESET_XSLT, Updates.runPatch(patchID, conn));
			} catch (SQLException e) {
				SQLException sqle = e;
				resp = new XMLResponse(ResponseType.RESET_XSLT, false);
				resp.addMessage(db.getDbID() + "(" + db.getVersion() + ")");
				resp.addMessage("SQLState:   " + (sqle).getSQLState());
				resp.addMessage("Severity: " + (sqle).getErrorCode());
				resp.addMessage("Message:  " + (sqle).getMessage());
			} catch (Exception e) {
				resp = new XMLResponse(ResponseType.RESET_XSLT, false);
				resp.addMessage(e.toString());
			} catch (Throwable e) {
				resp = new XMLResponse(ResponseType.RESET_XSLT, false);
				resp.addMessage(e.toString());
			} finally {
				db.getConnectionPool().returnConnection(conn);
			}
			result.output.append(resp.toXML());
		}
		return result;
	}

}
