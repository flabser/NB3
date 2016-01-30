package kz.flabs.servlets.admin;


import kz.flabs.exception.PortalException;
import kz.flabs.webrule.handler.TriggerType;

public class HandlerResult {
	TriggerType type;
	String scriptResult;
	String console;
	Exception error;
	boolean status = true;

	HandlerResult(TriggerType type){
		this.type = type;
	}

	void setError(Exception e){
		error = e;
		status = false;
	}
	
	public String toXML() {
		StringBuffer result = new StringBuffer(100);
		if (status){		
			result.append("<handler type=\"" + type + "\" status=\"ok\">");
			result.append("<console_output><![CDATA[" + console + "]]></console_output>");
			result.append("<string_result><![CDATA[" + scriptResult + "]]></string_result>");	
			result.append("</handler>");
		}else{
			result.append("<handler type=\"" + type + "\" status=\"error\">");
			result.append("<console_output>" + console + "</console_output>");
			result.append("<error>" + PortalException.errorMessage(error) + "</error>");	
			result.append("</handler>");
			
		}
		return result.toString();
	}

	
	public String toString() {
		return toXML();
	}
}
