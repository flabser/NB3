package kz.flabs.scriptprocessor.form.queryopen;

import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script.*;

public interface IQueryOpenScript {	
	void setAppEnv(AppEnv env);
	void setSession(_Session ses);	
	void setFormData(_WebFormData formData);
	void setCurrentLang(Vocabulary vocabulary, String lang);
	void stopOpen();
	PublishResult process1();
	PublishResult process2();
	void setDocument(_Document doc);
	
}
