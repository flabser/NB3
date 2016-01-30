package kz.flabs.appdaemon;

import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.synchronizer.SynchroGlobalSetting;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;

public class Sync extends AbstractDaemon {
	private SynchroGlobalSetting setting;
	
	

	@Override
	public int process(IProcessInitiator env) {
		if (setting.isSneakernetOn == RunMode.ON){
			
		}
		return 0;
	}

}
