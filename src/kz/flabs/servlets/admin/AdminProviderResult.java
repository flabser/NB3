package kz.flabs.servlets.admin;

import kz.flabs.servlets.PublishAsType;

public class AdminProviderResult {
	public PublishAsType publishAs;
	public String xslt;
	
	String output;
	
	AdminProviderResult(PublishAsType publishAs, String xslt){
		this.publishAs = publishAs;
		this.xslt = xslt;		
	}
	
	public void setOutput(String output){
		this.output = output;
	}
	
	public String getOutput(){
		return output;
	}
	
}
