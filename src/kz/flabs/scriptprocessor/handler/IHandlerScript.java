package kz.flabs.scriptprocessor.handler;

import java.util.Map;

import kz.flabs.localization.Vocabulary;
import kz.flabs.util.PageResponse;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;

public interface IHandlerScript {	
	void setSession(_Session ses);	
	@Deprecated
	void setFormData(Map<String, String[]> formData);
	void setWebFormData(_WebFormData formData);
	@Deprecated
	void setCurrentLang(String string, Vocabulary vocabulary);
	PageResponse process();
	PageResponse run();
	
}
