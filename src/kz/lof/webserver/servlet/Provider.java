package kz.lof.webserver.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
import kz.lof.env.EnvConst;
import kz.lof.exception.ApplicationException;
import kz.nextbase.script._Session;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;

import org.apache.http.HttpStatus;

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

			if (env != null) {
				PageRule rule = env.ruleProvider.getRule(id);
				if (rule != null) {
					jses = request.getSession(false);
					ses = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);

					Page page = new Page(env, ses, rule);
					Map<String, String[]> formData = request.getParameterMap();
					if (onlyXML != null) {
						result = page.getPageContent(formData, request.getMethod());
						result.publishAs = PublishAsType.XML;
					} else {
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
							result.publishAs = PublishAsType.JSON;
						}
					}

					response.setStatus(result.getHttpStatus());

					if (result.publishAs == PublishAsType.HTML) {
						if (result.disableClientCache) {
							disableCash(response);
						}

						String outputContent = result.toCompleteXML();
						File xslFile = new File(env.globalSetting.defaultSkin.path + File.separator + rule.xsltFile);
						if (xslFile.exists()) {
							response.setContentType("text/html");
							new SaxonTransformator().toTrans(response, xslFile, outputContent);
						} else {
							response.setContentType("text/xml;charset=utf-8");
							PrintWriter out = response.getWriter();
							out.println(outputContent);
							out.close();
						}
					} else if (result.publishAs == PublishAsType.JSON) {
						response.setContentType("application/json;charset=utf-8");
						PrintWriter out = response.getWriter();
						String json = result.getJSON();
						System.out.println(json);
						out.println(json);
						out.close();
					} else if (result.publishAs == PublishAsType.XML) {
						if (result.disableClientCache) {
							disableCash(response);
						}
						response.setContentType("text/xml;charset=utf-8");
						PrintWriter out = response.getWriter();
						out.println(result.toCompleteXML());
						out.close();
					}
				} else {
					throw new ApplicationException(context.getServletContextName(), id + " rule has not found");
				}

			} else {
				throw new ApplicationException(context.getServletContextName(), "application context has not found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
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
