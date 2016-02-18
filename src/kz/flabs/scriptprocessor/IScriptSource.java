package kz.flabs.scriptprocessor;

import java.sql.Connection;
import java.util.Map;

import kz.lof.scripting._Session;

public interface IScriptSource {
	void setSession(_Session ses);

	void setLang(String lang);

	void setFormData(Map<String, String[]> formData);

	void setConnection(Connection conn);

	void setUser(String user);

	String[] simpleProcess();

	String[] sessionProcess();

	String[] sessionLangProcess();

	String[] documentProcess();

	String[] documentLangProcess();

	String providerHandlerProcess() throws Exception;

	String schedulerHandlerProcess() throws Exception;

	String patchHandlerProcess() throws Exception;

	String getConsoleOutput();
}
