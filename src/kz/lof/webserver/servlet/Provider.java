package kz.lof.webserver.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.SaxonTransformator;
import kz.flabs.webrule.page.PageRule;
import kz.nextbase.script._Session;
import kz.pchelka.env.EnvConst;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;

public class Provider extends HttpServlet {

	private static final long serialVersionUID = 2352885167311108325L;
	private AppEnv env;
	private ServletContext context;

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			context = config.getServletContext();
			env = (AppEnv) context.getAttribute(EnvConst.APP_ATTR);
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doPost(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession jses = null;
		_Session ses = null;
		PageOutcome result = null;

		try {
			request.setCharacterEncoding(EnvConst.SUPPOSED_CODE_PAGE);
			String onlyXML = request.getParameter("onlyxml");
			String id = request.getParameter("id");
			if (id == null) {
				id = EnvConst.DEFAULT_PAGE;
			}

			if (env != null) {
				PageRule rule = env.ruleProvider.getRule(id);
				if (rule != null) {
					jses = request.getSession(false);
					ses = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);

					if (onlyXML != null) {
						result.publishAs = PublishAsType.XML;
					}

					HashMap<String, String[]> formData = new HashMap<String, String[]>();
					Page page = new Page(env, ses, rule);
					if (request.getMethod().equalsIgnoreCase("GET")) {
						switch (rule.caching) {
						case NO_CACHING:
							result = page.getPageContent(formData, "GET");
							break;
						case CACHING_IN_USER_SESSION_SCOPE:
							result = ses.getCachedPage(page, formData);
							break;
						case CACHING_IN_APPLICATION_SCOPE:
							result = env.getCachedPage(page, formData);
							break;
						case CACHING_IN_SERVER_SCOPE:
							result = new Environment().getCachedPage(page, formData);
							break;

						default:
							result = page.getPageContent(formData, "GET");
						}
					} else {
						result = page.getPageContent(formData, "POST");
					}

					if (result.publishAs == PublishAsType.HTML) {
						if (result.disableClientCache) {
							disableCash(response);
						}

						String outputContent = result.toCompleteXML(ses);
						File xslFile = new File(rule.xsltFile);
						if (xslFile.exists()) {
							response.setContentType("text/html");
							new SaxonTransformator().toTrans(response, xslFile, outputContent);
						} else {
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
						PrintWriter out = response.getWriter();
						out.println(result.toCompleteXML(ses));
						out.close();
					}
				} else {
					return;
				}

			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
			/*
			 * ApplicationException ae = new
			 * ApplicationException(env.templateType, e.toString(), e);
			 * response.setStatus(ae.getCode());
			 * response.getWriter().println(ae.getHTMLMessage());
			 */
		}
	}

	private void disableCash(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache, must-revalidate, private, no-store, s-maxage=0, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}
}
