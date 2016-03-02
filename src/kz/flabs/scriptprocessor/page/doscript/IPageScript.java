package kz.flabs.scriptprocessor.page.doscript;

import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.webserver.servlet.PageOutcome;

public interface IPageScript {
	void setSession(_Session ses);

	void setPageOutcome(PageOutcome outcome);

	void setFormData(_WebFormData formData);

	PageOutcome processCode(String method);

}
