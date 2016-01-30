package kz.nextbase.script.events;

import kz.flabs.localization.LanguageType;
import kz.flabs.scriptprocessor.page.doscript.AbstractPage;
import kz.nextbase.script._Exception;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public abstract class _DoPage extends AbstractPage {
	@Override
	public abstract void doGET(_Session session, _WebFormData formData, LanguageType lang) throws _Exception;

	@Override
	public abstract void doPOST(_Session session, _WebFormData formData, LanguageType lang) throws _Exception;
}
