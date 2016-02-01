package kz.flabs.servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.XSLTFileNotFoundException;
import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Skin;
import kz.pchelka.server.Server;

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

	public boolean prepareXSLT(AppEnv env, Skin skin, String xslt) throws XSLTFileNotFoundException {
		String clientXSLT = request.getParameter("xslt");
		String xsltFile = "";
		boolean result;

		if (clientXSLT != null) {
			xsltFile = clientXSLT;
		} else {
			xsltFile = xslt;
		}

		if (skin != null) {
			xslFile = env.xsltFileMap.get(skin.id + "#" + xsltFile);
			if (xslFile == null) {
				xslFile = new File(skin.path + File.separator + xsltFile);

				if (xslFile.exists()) {
					String xsltFilePath = skin.id + "#" + xsltFile;
					env.xsltFileMap.put(xsltFilePath, xslFile);
					result = true;
				} else {
					xslFile = new File(env.globalSetting.defaultSkin.path + File.separator + xsltFile);
					if (xslFile.exists()) {
						Server.logger.warningLogEntry("file " + xslFile + " has not found, will be used a file of a default skin");
						setDefaultSkin(env.globalSetting.defaultSkin);
						skin = env.globalSetting.defaultSkin;
						env.xsltFileMap.put(env.globalSetting.defaultSkin.path + File.separator + xsltFile, xslFile);
						result = true;
					} else {

						PrintWriter writer;
						try {
							writer = new PrintWriter(xslFile.getAbsolutePath(), "UTF-8");
							writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
							writer.println("<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">");
							writer.println("<xsl:output method=\"html\" encoding=\"utf-8\"/>");
							writer.println("<xsl:template match=\"/\">");
							writer.println("<!-- it was generated programmatically -->");
							writer.println("<html><body> <xmp><xsl:copy-of select=\".\"/></xmp></body></html>");
							writer.println("</xsl:template>");
							writer.println("</xsl:stylesheet>");
							writer.close();
							throw new XSLTFileNotFoundException(xslFile.getAbsolutePath());
						} catch (FileNotFoundException | UnsupportedEncodingException e) {
							throw new XSLTFileNotFoundException(xslFile.getAbsolutePath());
						}

					}
				}
			} else {
				result = true;
			}
		} else {
			xslFile = new File(env.globalSetting.defaultSkin.path + File.separator + xsltFile);
			if (xslFile.exists()) {
				Server.logger.warningLogEntry("failed to use the selected skin, will be used a default skin");
				skin = env.globalSetting.defaultSkin;
				setDefaultSkin(skin);
				env.xsltFileMap.put(env.globalSetting.defaultSkin.path + File.separator + xsltFile, xslFile);
				result = true;
			} else {
				throw new XSLTFileNotFoundException(xslFile.getAbsolutePath());
			}
		}
		jses.setAttribute("skin", skin.id);
		return result;
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