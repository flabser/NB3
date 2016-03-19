package kz.flabs.servlets.admin;

import java.io.File;

import kz.flabs.exception.XSLTFileNotFoundException;
import kz.flabs.servlets.ProviderOutput;

public class AdminProviderOutput extends ProviderOutput {
	private String dbID;
	private String element;
	private static final String adminXSLTPath = "." + File.separator + "webapps" + File.separator + "Administrator" + File.separator + "xslt"
	        + File.separator;

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

		return xmlTextUTF8Header + "<request type=\"" + type + "\" lang=\"RUS\" element=\"" + element + "\" id=\"" + id
		        + "\"   skin=\"pchelka\" dbid=\"" + dbID + "\" " + "localusername=\"" + localUserName + "\">" + "<history>" + historyXML
		        + "</history>" + new AdminOutline().getOutlineAsXML() + output + "</request>";
	}

}
