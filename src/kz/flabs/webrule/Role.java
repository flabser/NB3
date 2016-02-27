package kz.flabs.webrule;

import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.appenv.AppEnv;

import org.w3c.dom.Node;

public class Role {
	public String name = "unknown";
	public String appID;
	public String description;
	public RunMode isOn = RunMode.ON;
	public boolean isValid;
	public boolean active = false;

	@Deprecated
	public Role(String name, String description) {
		appID = "System";
		this.name = name;
		this.description = description;
	}

	public Role(String name, String appID, String description) {
		this.appID = appID;
		this.name = name;
		this.description = description;
	}

	Role(Node node, String appID) {
		try {
			this.appID = appID;
			name = XMLUtil.getTextContent(node, "@id", false);
			description = XMLUtil.getTextContent(node, ".", false);

			if (XMLUtil.getTextContent(node, "@mode", false).equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				isValid = false;
			}

			if (XMLUtil.getTextContent(node, "@default", false).equalsIgnoreCase("on")) {
				active = true;
			}

			isValid = true;
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(this.getClass().getSimpleName(), e);
		}
	}

	public String toXML() {
		return "<ison>" + isOn + "</ison><name>" + name + "</name><app>" + appID + "</app>" + "<description>" + description
		        + "</description><ruleprovider></ruleprovider>";
	}
}
