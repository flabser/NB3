package kz.flabs.scriptprocessor.page.doscript;

import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public interface IPageScript {
	void setSession(_Session ses);

	void setFormData(_WebFormData formData);

	PageOutcome processCode(String method);

	void setCurrentLang(Vocabulary vocabulary, LanguageType lang);

}
