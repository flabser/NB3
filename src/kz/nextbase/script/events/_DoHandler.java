package kz.nextbase.script.events;

import java.util.Map;
import kz.flabs.scriptprocessor.handler.AbstractHandler;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public abstract class _DoHandler extends AbstractHandler {
	
	@Deprecated
	public void doHandler(_Session session, Map<String, String[]> formData, String lang){
		
	}
	
	public abstract void doHandler(_Session session, _WebFormData formData);
	
}

