package kz.nextbase.script.events;

import java.util.Map;

import kz.flabs.scriptprocessor.handler.AbstractHandler;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;

public abstract class _DoHandler extends AbstractHandler {
	
	@Deprecated
	public void doHandler(_Session session, Map<String, String[]> formData, String lang){
		
	}
	
	public abstract void doHandler(_Session session, _WebFormData formData);
	
}

