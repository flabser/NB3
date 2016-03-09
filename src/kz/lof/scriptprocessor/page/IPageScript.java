package kz.lof.scriptprocessor.page;

import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;

public interface IPageScript {
	void setSession(_Session ses);

	void setOutcome(PageOutcome outcome);

	void setFormData(_WebFormData formData);

	PageOutcome processCode(String method);

}
