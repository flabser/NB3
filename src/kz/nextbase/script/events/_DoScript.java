package kz.nextbase.script.events;

import kz.flabs.scriptprocessor.page.doscript.AbstractPageScript;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public abstract class _DoScript extends AbstractPageScript {
	public abstract void doProcess(_Session session, _WebFormData formData, String lang);
}
