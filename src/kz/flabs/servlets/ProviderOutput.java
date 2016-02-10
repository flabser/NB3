package kz.flabs.servlets;

import java.io.File;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Skin;

public class ProviderOutput {
	public File xslFile;
	public boolean isValid;

	protected static final String xmlTextUTF8Header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	protected String type;
	public BrowserType browser;
	protected StringBuffer output;
	protected String historyXML = "";
	protected UserSession userSession;
	protected HttpSession jses;
	protected String id;
	protected String title;

	private HttpServletRequest request;
	private HttpServletResponse response;

	public ProviderOutput(String type, String id, StringBuffer output, HttpServletRequest request, HttpServletResponse response,
	        UserSession userSession, HttpSession jses, String title, boolean addHistory) throws UserException {
		this.type = type;
		this.id = id;
		this.output = output;
		this.request = request;
		this.response = response;
		this.jses = jses;
		this.title = title;

		browser = userSession.browserType;

		this.userSession = userSession;

		if (addHistory) {
			addHistory();
		}
		if (userSession.history != null) {
			for (UserSession.HistoryEntry entry : userSession.history.getEntries()) {
				historyXML += "<entry type=\"" + entry.type + "\" title=\"" + entry.title + "\">" + XMLUtil.getAsTagValue(entry.URLforXML)
				        + "</entry>";
			}
		}

	}

	public String getPlainText() {
		return output.toString();
	}

	public String getStandartOutput() {
		String localUserName = "";
		localUserName = userSession.currentUser.getUserName();

		String queryString = request.getQueryString();
		if (queryString != null) {
			queryString = "querystring=\"" + queryString.replace("&", "&amp;") + "\"";
		} else {
			queryString = "";
		}

		return xmlTextUTF8Header + "<request " + queryString + " lang=\"" + userSession.lang + "\" id=\"" + id + "\" " + "useragent=\"" + browser
		        + "\"  userid=\"" + userSession.currentUser.getUserID() + "\" username=\"" + localUserName + "\">" + output + "</request>";
	}

	public String getStandartUTF8Output() {
		String localUserName = "";
		localUserName = userSession.currentUser.getUserName();

		String queryString = request.getQueryString();
		if (queryString != null) {
			queryString = "querystring=\"" + queryString.replace("&", "&amp;") + "\"";
		} else {
			queryString = "";
		}

		String outputContent = xmlTextUTF8Header + "<request " + queryString + " lang=\"" + userSession.lang + "\" id=\"" + id + "\" "
		        + "useragent=\"" + browser + "\" userid=\"" + "\" " + "username=\"" + localUserName + "\">" + output + "</request>";

		return outputContent;
	}

	protected void addHistory() throws UserException {
		String ref = request.getRequestURI() + "?" + request.getQueryString();
		userSession.addHistoryEntry(type, ref, title);
	}

	private void setDefaultSkin(Skin skin) {
		Cookie loginCook = new Cookie("skin", skin.id);
		loginCook.setMaxAge(604800);
		response.addCookie(loginCook);
		userSession.skin = skin.id;
	}

}