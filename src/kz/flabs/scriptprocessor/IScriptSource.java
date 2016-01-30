package kz.flabs.scriptprocessor;

import java.sql.Connection;
import java.util.Map;

import kz.nextbase.script.*;

public interface IScriptSource {
	void setSession(_Session ses);	
	void setDocument(_Document doc);
	void setLang(String lang);
	void setDocumentCollection(_DocumentCollection col);
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
