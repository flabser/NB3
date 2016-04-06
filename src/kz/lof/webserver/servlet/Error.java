package kz.lof.webserver.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kz.flabs.exception.TransformatorException;
import kz.flabs.servlets.SaxonTransformator;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.server.Server;
import net.sf.saxon.s9api.SaxonApiException;

public class Error extends HttpServlet {
	private static final long serialVersionUID = 1207733369437122383L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		String type = request.getParameter("type");
		String msg = request.getParameter("msg");
		String xslt = Environment.getKernelDir() + "xsl" + File.separator + "error.xsl";
		try {
			request.setCharacterEncoding(EnvConst.SUPPOSED_CODE_PAGE);
			String outputContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

			if (type.equals("ws_auth_error")) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				xslt = Environment.getKernelDir() + "xsl" + File.separator + "authfailed.xsl";
				outputContent = outputContent + "<request><error type=\"authfailed\"><message>" + msg + "</message><version>" + Server.serverVersion
				        + "</version></error></request>";
			} else if (type.equals("application_was_restricted")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				msg = "work with the application was restricted";
				outputContent = outputContent + "<request><error type=\"" + type + "\"><message>" + msg + "</message><version>"
				        + Server.serverVersion + "</version></error></request>";
			} else if (type.equals("default_url_not_defined")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				msg = "default URL has not defined in global setting";
				outputContent = outputContent + "<request><error type=\"" + type + "\"><message>" + msg + "</message><version>"
				        + Server.serverVersion + "</version></error></request>";
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				outputContent = outputContent + "<request><error type=\"" + type + "\"><message>" + msg + "</message><version>"
				        + Server.serverVersion + "</version></error></request>";
			}

			if (request.getParameter("as") != null) {
				response.setContentType("text/xml;charset=utf-8");
				PrintWriter out = response.getWriter();
				out.println(outputContent);
				out.close();
			} else {
				response.setContentType("text/html");
				File errorXslt = new File(xslt);
				new SaxonTransformator().toTrans(response, errorXslt, outputContent);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SaxonApiException e) {
			e.printStackTrace();
		} catch (TransformatorException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

}
