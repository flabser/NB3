package kz.nextbase.script.events;

import kz.flabs.scriptprocessor.form.querysave.AbstractQuerySave;
import kz.nextbase.script.*;

public abstract class _FormQuerySave extends AbstractQuerySave{
	public abstract void doQuerySave(_Session ses, _Document doc,_WebFormData webFormData, String lang);
	
}
