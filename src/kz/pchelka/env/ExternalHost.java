package kz.pchelka.env;

import org.w3c.dom.Node;

import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.server.Server;

public class ExternalHost {
	public String host;
	public String id;
	public String name;
	public ExternalHostType type;
	public String server;
	public String user;
	public String pwd;
	public String db;
	public RunMode isOn = RunMode.ON;
	public boolean isValid;

	ExternalHost(Node node) {
		try {
			host = XMLUtil.getTextContent(node, ".", false);
			id = XMLUtil.getTextContent(node, "@id", false);
			name = XMLUtil.getTextContent(node, "@name", false);
			server = XMLUtil.getTextContent(node, "@server", false);
			user = XMLUtil.getTextContent(node, "@user", false);
			pwd = XMLUtil.getTextContent(node, "@pwd", false);
			db = XMLUtil.getTextContent(node, "@db", false);
			type = ExternalHostType.valueOf(XMLUtil.getTextContent(node, "@type", true, "UNDEFINED", false));
			isValid = true;
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);

		}
	}

	ExternalHost(String id, String host, String name) {
		this.id = id;
		this.host = host;
		this.name = name;
		this.isOn = RunMode.ON;
		this.type = ExternalHostType.WEB_SERVICES_PROVIDER;
		this.isValid = true;
	}

	@Override
	public String toString() {
		return "id=" + id + ", host=" + host + ", type= " + type + ", isOn=" + isOn + ", isvalid=" + isValid;
	}

}
