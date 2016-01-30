package kz.flabs.scriptprocessor.form.querysave;

import kz.flabs.localization.Vocabulary;
import kz.nextbase.script.*;

public interface IQuerySaveScript {	
	void setSession(_Session ses);	
	void setDocument(_Document doc);
	void setWebFormData(_WebFormData webFormData);
	@Deprecated
	void setUser(String user);
	void setCurrentLang(Vocabulary vocabulary, String lang);
	void stopSave();
	QuerySaveResult process();
	
}
