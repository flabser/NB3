package kz.nextbase.script.events;

import kz.flabs.scriptprocessor.form.queryopen.AbstractQueryOpen;
import kz.nextbase.script.*;

public abstract class _FormQueryOpen extends AbstractQueryOpen{
	
	public abstract void doQueryOpen(_Session session, _WebFormData webFormData, String lang);
	public abstract void doQueryOpen(_Session session,  _Document doc, _WebFormData webFormData, String lang);
	
}
