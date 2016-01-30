package kz.flabs.webrule.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.handler.TriggerType;
import kz.pchelka.scheduler.DaemonType;
import kz.pchelka.scheduler.IProcessInitiator;

public interface IScheduledProcessRule {
	void init(IProcessInitiator owner);
	IProcessInitiator getOwner();
	String getClassName();
	void setScheduleMode(RunMode isOn);
	RunMode getScheduleMode();
	TriggerType getTriggerType();
	String getProcessID();
	ScheduleType getScheduleType();
	DaemonType getDaemonType();
	Calendar getStartTime();
	ArrayList<DaysOfWeek> getDaysOfWeek();
	void setNextStartTime(Calendar time);	
	int getMinuteInterval();
	boolean scriptIsValid();
	
	
}
