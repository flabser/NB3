package kz.pchelka.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.IScheduledProcessRule;
import kz.flabs.webrule.scheduler.ScheduleSettings;
import kz.flabs.webrule.scheduler.ScheduleType;

public abstract class DaemonRule implements IScheduledProcessRule{
	public String handlerClass;
	public ScheduleSettings scheduleSettings;

	protected IProcessInitiator owner;	
	protected DaemonType daemonType;
	
	protected RunMode isOn;
	protected Calendar nextStart;
	
	
	@Override
	abstract public void init(IProcessInitiator owner);
	
	
	@Override
	public String getProcessID() {
		return getDaemonType() + "_" + getClassName();
		
	}

	
	@Override
	public IProcessInitiator getOwner() {
		return owner;
	}
	
	@Override
	public String getClassName() {
		return handlerClass;
	}

	@Override
	public void setScheduleMode(RunMode isOn) {
		this.isOn = isOn;		
	}

	@Override
	public RunMode getScheduleMode() {		
		return isOn;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.SCHEDULER;
	}

	

	@Override
	public ScheduleType getScheduleType() {
		return scheduleSettings.schedulerType;
	}

	@Override
	public DaemonType getDaemonType() {
		return daemonType;
	}

	@Override
	public Calendar getStartTime(){
		if (nextStart == null){
			nextStart = new GregorianCalendar();
			nextStart.add(Calendar.MINUTE, 1);
		}
		return nextStart;
		
	}

	@Override
	public void setNextStartTime(Calendar time) {
		scheduleSettings.setNextStart(time);
		nextStart = time;
	}

	@Override
	public int getMinuteInterval() {
		return scheduleSettings.minInterval;
	}

	@Override
	public ArrayList<DaysOfWeek> getDaysOfWeek() {
		return null;
	}
	
	public String toString(){
		 return getProcessID();
	}

	
}
