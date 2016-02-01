package kz.flabs.servlets.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

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
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.holiday.HolidayCollection;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.PortalException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.servlets.ProviderExceptionType;
import kz.flabs.servlets.ProviderResult;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.ServletUtil;
import kz.flabs.servlets.sitefiles.AttachmentHandler;
import kz.flabs.users.User;
import kz.flabs.webrule.IRule;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.page.PageRule;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ViewEntryCollection;
import kz.pchelka.env.Environment;
import kz.pchelka.log.LogFiles;
import kz.pchelka.scheduler.BackgroundProcCollection;
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

				} else if (type.equalsIgnoreCase("delete_glossary")) {

				} else if (type.equalsIgnoreCase("get_glossary")) { // get

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

			if (result.publishAs == PublishAsType.HTML) {
				if (result.disableClientCache) {
					// disableCash(response);
				}

				response.setContentType("text/html");

			} else if (result.publishAs == PublishAsType.XML) {
				if (result.disableClientCache) {
					// disableCash(response);
				}
				response.setContentType("text/xml;charset=utf-8");

				// System.out.println(outputContent);
				PrintWriter out = response.getWriter();

				out.close();
			} else if (result.publishAs == PublishAsType.TEXT) {
				if (result.disableClientCache) {
					// disableCash(response);
				}

				response.setContentType("text/text;charset=utf-8");

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
		String content = "";
		AppEnv env = null;
		IDatabase db = null;
		if (app != null && !"".equalsIgnoreCase(app)) {
			env = Environment.getApplication(app);
			dbID = env.getDataBase().getDbID();
			db = DatabaseFactory.getDatabaseByName(dbID);
		} else if (dbID != null && !"".equalsIgnoreCase(dbID)) {
			db = DatabaseFactory.getDatabaseByName(dbID);
		} else {

		}

		int count = 0;
		int page = ServletUtil.getPage(request);

		if (element.equalsIgnoreCase("cfg")) {
			result.xslt = "forms" + File.separator + "cfg.xsl";

		} else if (element.equalsIgnoreCase("console")) {
			result.xslt = "views" + File.separator + "console.xsl";
			// result.output.append("<console sesid=\"" + jses.getId() +
			// "\" server=\"" + Environment.remoteConsoleServer + "\" port=\"" +
			// Environment.remoteConsolePort + "\"></console>");
		} else if (element.equalsIgnoreCase("logs")) {
			LogFiles logs = new LogFiles();
			result.xslt = "views" + File.separator + "logs_list.xsl";
			count = logs.getCount();

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
		} else if (element.equalsIgnoreCase("calendar")) {
			result.xslt = "views" + File.separator + "calendar_list.xsl";
			HolidayCollection holidays = Environment.systemBase.getHolidayCol(Calendar.getInstance().get(Calendar.YEAR), page, pageSize);
			count = holidays.holidays.size();

		} else if (element.equalsIgnoreCase("activity")) {

		} else if (element.equalsIgnoreCase("document_activity")) {

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

		AppEnv appEnv = null;
		String dbID = "";
		if (app != null) {
			appEnv = Environment.getApplication(app);
			dbID = Environment.getApplication(app).getDataBase().getDbID();

		} else {

		}

		result.output.append("<document>");
		if (element.equalsIgnoreCase("cfg")) {
			result.xslt = "forms" + File.separator + "cfg.xsl";

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

		} else if (element.equalsIgnoreCase("glossary")) {
			result.xslt = "forms" + File.separator + "document.xsl";

		}
		result.output.append("</document>");
		return result;

	}

}
