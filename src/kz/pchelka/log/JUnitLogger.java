package kz.pchelka.log;

import java.util.logging.Logger;



public class JUnitLogger implements ILogger {
	
	
	
	public void errorLogEntry(String logtext) {
			
	}

  
	public void errorLogEntry(Exception exception) {
		
	}

	public void errorLogEntry(String agent, String logtext) {
				
	}

	
	public void errorLogEntry(String agent, String logtext, String searchKey) {
				
	}

	public void errorLogEntry(String agent, Exception exception) {
	
	}

	public void errorLogEntry(String agent, Exception exception,String searchKey) {
	
	}
	
	
	public void normalLogEntry(String logtext) {
		assert (logtext) != null;
	}


	public void normalLogEntry(String agent, String logtext) {
				
	}

	
	public void verboseLogEntry(String logtext) {
				
	}

	
	public void verboseLogEntry(String agent, String logtext) {
				
	}


	public void warningLogEntry(String logtext) {
				
	}


	public void warningLogEntry(String agent, String logtext) {
				
	}


	@Override
	public void fatalLogEntry(String logtext) {
				
	}


	@Override
	public void errorLogEntry(Throwable exception) {
				
	}

}
