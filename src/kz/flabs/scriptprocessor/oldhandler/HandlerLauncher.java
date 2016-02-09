package kz.flabs.scriptprocessor.oldhandler;

import kz.flabs.scriptprocessor.handler.HandlerScriptProcessor;
import kz.lof.server.Server;

@Deprecated
public class HandlerLauncher extends Thread {
	private HandlerScriptProcessor handler;
	private String script;
	
	@Deprecated
	public HandlerLauncher(HandlerScriptProcessor handler, String script){
		this.handler = handler;
		this.script = script;
	}
	
	public void run(){
		try {
			handler.process(script, false);
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}
}
