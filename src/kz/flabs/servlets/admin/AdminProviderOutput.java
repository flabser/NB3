package kz.flabs.servlets.admin;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.exception.XSLTFileNotFoundException;
import kz.flabs.servlets.ProviderOutput;
import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;

public class AdminProviderOutput extends ProviderOutput {
	private String dbID;
	private String element;
	private static final String adminXSLTPath = "." + File.separator + "webapps" + File.separator + "Administrator" + File.separator + "xslt"
	        + File.separator;

	AdminProviderOutput(String type, String element, String id, StringBuffer output, HttpServletRequest request, HttpServletResponse response,
	        UserSession userSession, HttpSession jses, String dbID) throws UserException {
		super(type, element, output, request, response, userSession, jses, "element=" + element + ", id=" + id, false);
		this.dbID = dbID;
		this.element = element;
	}

	public boolean prepareXSLT(String xsltFileName) throws XSLTFileNotFoundException {

		String xsltFilePath = adminXSLTPath + File.separator + xsltFileName;
		xslFile = new File(xsltFilePath);
		if (!xslFile.exists()) {
			throw new XSLTFileNotFoundException(xslFile.getAbsolutePath());
		}
		return true;
	}

	@Override
	public String getStandartOutput() {
		String localUserName = "";

		if (userSession != null) {
			localUserName = "user";
		} else {
			localUserName = userSession.currentUser.getUserID();
		}

		return xmlTextUTF8Header + "<request type=\"" + type + "\" lang=\"RUS\" element=\"" + element + "\" id=\"" + id + "\" " + "useragent=\""
		        + browser + "\"  skin=\"pchelka\" dbid=\"" + dbID + "\" userid=\"" + userSession.currentUser.getUserID() + "\" " + "localusername=\""
		        + localUserName + "\">" + "<history>" + historyXML + "</history>" + new AdminOutline().getOutlineAsXML() + output + "</request>";
	}

}
