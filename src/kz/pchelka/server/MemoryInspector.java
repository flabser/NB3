package kz.pchelka.server;

import java.util.TimerTask;
import kz.pchelka.log.*;

public class MemoryInspector extends TimerTask {	
	private String agent = "Memory Inspector";	
	private int maxHeap;
	
	MemoryInspector(ILogger logger){		
		Runtime rt = Runtime.getRuntime();		
		maxHeap = (int) (rt.totalMemory()/1024/1024);
		logger.normalLogEntry(agent,"mem (total=" + (maxHeap)+"Mb, max="+(rt.maxMemory()/1024/1024)+"Mb)");
		Thread.currentThread().setName(Server.serverVersion + " Memory Inspector");	
	}
	
	public void run(){
		try{			
			Runtime rt = Runtime.getRuntime();		
			int free = (int) (rt.freeMemory()/1024/1024);			
		} catch(Exception e) {
			e.printStackTrace();
			Server.logger.errorLogEntry(agent,e);			
		}  
	}
}
