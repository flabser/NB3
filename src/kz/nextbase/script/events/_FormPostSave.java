package kz.nextbase.script.events;

import kz.flabs.scriptprocessor.form.postsave.AbstractPostSave;
import kz.nextbase.script.*;

public abstract class _FormPostSave extends AbstractPostSave{
	public abstract void doPostSave(_Session ses, _Document doc);
	
}
