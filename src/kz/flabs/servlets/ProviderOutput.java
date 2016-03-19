package kz.flabs.servlets;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.users.UserException;

public class ProviderOutput {
	public File xslFile;
	public boolean isValid;

	protected static final String xmlTextUTF8Header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	protected String type;
	protected StringBuffer output;
	protected String historyXML = "";

	protected HttpSession jses;
	protected String id;
	protected String title;

	private HttpServletRequest request;
	private HttpServletResponse response;

	public String getPlainText() {
		return output.toString();
	}

	public String getStandartOutput() {
		String localUserName = "";
		// localUserName = userSession.currentUser.getUserName();

		String queryString = request.getQueryString();
		if (queryString != null) {
			queryString = "querystring=\"" + queryString.replace("&", "&amp;") + "\"";
		} else {
			queryString = "";
		}

		return xmlTextUTF8Header + "<request " + queryString + "  id=\"" + id + "\"   username=\"" + localUserName + "\">" + output + "</request>";
	}

	public String getStandartUTF8Output() {
		String localUserName = "";

		String queryString = request.getQueryString();
		if (queryString != null) {
			queryString = "querystring=\"" + queryString.replace("&", "&amp;") + "\"";
		} else {
			queryString = "";
		}

		String outputContent = xmlTextUTF8Header + "<request " + queryString + "  id=\"" + id + "\"  userid=\"" + "\" " + "username=\""
		        + localUserName + "\">" + output + "</request>";

		return outputContent;
	}

	protected void addHistory() throws UserException {
		String ref = request.getRequestURI() + "?" + request.getQueryString();

	}

}