package kz.pchelka.daemon.system;

import java.util.Calendar;

import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.scheduler.ScheduleType;
import kz.pchelka.scheduler.DaemonRule;
import kz.pchelka.scheduler.DaemonType;
import kz.pchelka.scheduler.IProcessInitiator;

public abstract class SystemDaemonRule extends DaemonRule{
	

	@Override
	public void init(IProcessInitiator owner) {
		this.owner = owner;
		daemonType =  DaemonType.SYSTEM_SERVICE;	
		setScheduleMode(RunMode.ON);
	}

	@Override
	public void setNextStartTime(Calendar time) {
		nextStart = time;		
	}
	
	@Override
	public ScheduleType getScheduleType() {
		return ScheduleType.PERIODICAL;
	}
	
}
