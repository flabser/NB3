package kz.flabs.scriptprocessor.page.doscript;

import kz.flabs.localization.Vocabulary;
import kz.flabs.util.XMLResponse;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public interface IPageScript {
	void setSession(_Session ses);

	void setFormData(_WebFormData formData);

	void setCurrentLang(Vocabulary vocabulary, String lang);

	@Deprecated
	XMLResponse process();

	XMLResponse process(String method);

}
