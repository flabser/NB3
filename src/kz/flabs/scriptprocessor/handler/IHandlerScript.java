package kz.flabs.scriptprocessor.handler;

import java.util.Map;
import kz.flabs.localization.Vocabulary;
import kz.flabs.util.XMLResponse;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public interface IHandlerScript {	
	void setSession(_Session ses);	
	@Deprecated
	void setFormData(Map<String, String[]> formData);
	void setWebFormData(_WebFormData formData);
	@Deprecated
	void setCurrentLang(String string, Vocabulary vocabulary);
	XMLResponse process();
	XMLResponse run();
	
}
