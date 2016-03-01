package kz.lof.webserver.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.exception.RuleException;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.SaxonTransformator;
import kz.flabs.servlets.sitefiles.AttachmentHandler;
import kz.flabs.webrule.page.PageRule;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.exception.ApplicationException;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.server.Server;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

public class Provider extends HttpServlet {

	private static final long serialVersionUID = 2352885167311108325L;
	private ServletContext context;

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			context = config.getServletContext();
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
		HttpSession jses = request.getSession(false);
		_Session ses = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);
		AppEnv env = ses.getAppEnv();
		PageOutcome result = null;
		String id = request.getParameter("id");

		try {
			request.setCharacterEncoding(EnvConst.SUPPOSED_CODE_PAGE);
			PageRule rule = env.ruleProvider.getRule(id);
			Page page = new Page(env, ses, rule);
			String referrer = request.getHeader("referer");
			_WebFormData formData = new _WebFormData(request.getParameterMap(), referrer);
			if (formData.containsField("as")) {
				result = page.getPageContent(formData, request.getMethod());
				if (formData.getValue("as").equalsIgnoreCase("json")) {
					result.publishAs = PublishAsType.JSON;
				} else {
					result.publishAs = PublishAsType.XML;
				}
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
					result = page.getPageContent(formData, request.getMethod());
				}

				// TODO, need a HttpHeaders parser
				String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
				if (acceptHeader != null && acceptHeader.indexOf("application/json") != -1) {
					result.publishAs = PublishAsType.JSON;
				}
			}

			response.setStatus(result.getHttpStatus());
			if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				// TODO need to Improvement
				result.publishAs = PublishAsType.XML;
				ApplicationException e = new ApplicationException(context.getServletContextName(), result.getValue());
				throw e;
			}

			if (result.publishAs == PublishAsType.HTML) {
				if (result.disableClientCache) {
					disableCash(response);
				}

				String outputContent = result.toCompleteXML();
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
			} else if (result.publishAs == PublishAsType.JSON) {
				response.setContentType("application/json;charset=utf-8");
				PrintWriter out = response.getWriter();
				String json = result.getJSON();
				// System.out.println("json=" + json);
				// json = json.replaceAll("\u003d", "=").replaceAll("\u0026",
				// "&");
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
			} else if (result.publishAs == PublishAsType.OUTPUTSTREAM) {
				String disposition = "attachment";
				if (formData.containsField("disposition")) {
					disposition = formData.getValue("disposition");
				}
				AttachmentHandler attachHandler = new AttachmentHandler(request, response, true);
				attachHandler.publish(result.getFilePath(), result.getFileName(), disposition);
			}
		} catch (ApplicationException ae) {
			Server.logger.errorLogEntry(ae.toString());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ae.getHTMLMessage());
		} catch (RuleException e) {
			ApplicationException ae = new ApplicationException(env.appType, "rule_not_found (" + id + ")");
			Server.logger.errorLogEntry(ae.toString());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ae.getHTMLMessage());
		} catch (Exception e) {
			e.printStackTrace();
			ApplicationException ae = new ApplicationException(env.appType, e.toString(), e);
			Server.logger.errorLogEntry(ae.toString());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ae.getHTMLMessage());
		}
	}

	private void disableCash(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache, must-revalidate, private, no-store, s-maxage=0, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}
}
