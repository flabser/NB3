package kz.flabs.appdaemon;


import kz.pchelka.scheduler.DaemonRule;
import kz.pchelka.scheduler.DaemonType;
import kz.pchelka.scheduler.IProcessInitiator;


public class AppDaemonRule extends DaemonRule{

	
	
	@Override
	public void init(IProcessInitiator owner) {
		this.owner = owner;
		daemonType =  DaemonType.APPLICATION_SERVICE;		
	}

	@Override
	public boolean scriptIsValid() {	
		return true;
	}

	

}
