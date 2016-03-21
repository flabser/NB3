package kz.flabs.webrule;

import java.io.File;

import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.server.Server;

import org.w3c.dom.Node;

public class Skin {
	public String id = "unknown";
	public String name;
	public RunMode isOn = RunMode.ON;
	public boolean isValid;
	public boolean isDefault;
	public String path = "";
	public String errorPagePath;

	Skin(Node node) {
		try {
			id = XMLUtil.getTextContent(node, "@id", false);
			name = XMLUtil.getTextContent(node, ".", false);

			if (XMLUtil.getTextContent(node, "@mode", false).equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				isValid = false;
			} else {
				if (XMLUtil.getTextContent(node, "@default", false).equalsIgnoreCase("on")) {
					isDefault = true;
				}
			}

			path = XMLUtil.getTextContent(node, "@path", false);

			String errorPage = XMLUtil.getTextContent(node, "@errorpage", true);
			File errorFile = new File(path + File.separator + errorPage);
			if (errorFile.exists() && errorFile.isFile()) {
				errorPagePath = errorFile.getPath();
			} else {
				errorPagePath = "xsl" + File.separator + "error.xsl";
			}
			isValid = true;
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

	public String toXML() {
		return "<ison>" + isOn + "</ison><id>" + id + "</id><name>" + name + "</name><path>" + path + "</path>";
	}
}
