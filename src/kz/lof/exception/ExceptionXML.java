package kz.lof.exception;

import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;
import kz.lof.server.Server;

public class ExceptionXML {
	private String errorMessage = "";
	private int code = 0;
	private String location = "";
	private String type = "";
	private String servletName = "";
	private String exception = "";
	private String appType = "";

	public ExceptionXML(String errorMessage, int code, String location, String type, String servletName, String exception) {
		this.errorMessage = errorMessage;
		this.code = code;
		this.location = location;
		this.type = type;
		this.servletName = servletName;
		this.exception = exception;

	}

	public void setAppType(String t) {
		appType = t;
	}

	public String toXML(LanguageCode lang) {
		if (Environment.isDevMode()) {
			return "<?xml version = \"1.0\" encoding=\"" + EnvConst.DEFAULT_XML_ENC + "\"?><request><error><apptype>" + appType
			        + "</apptype><message>" + errorMessage + "</message><code>" + code + "</code><location>" + location + "</location><type>" + type
			        + "</type><name>" + servletName + "</name><exception><![CDATA[" + exception + "]]></exception><server>" + Server.serverTitle
			        + "</server></error></request>";
		} else {
			return "<?xml version = \"1.0\" encoding=\"" + EnvConst.DEFAULT_XML_ENC + "\"?><error><apptype>" + appType + "</apptype><message>"
			        + Environment.vocabulary.getWord("internal_server_error", lang) + "</message><code>" + code + "</code><location>" + location
			        + "</location><type>" + type + "</type><name>" + servletName + "</name><exception></exception><server>" + Server.serverTitle
			        + "</server></error>";
		}
	}

}
