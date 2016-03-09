package kz.flabs.webrule.scheduler;

import java.util.ArrayList;
import java.util.Calendar;

import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.handler.TriggerType;

public interface IScheduledProcessRule {

	String getClassName();

	void setScheduleMode(RunMode isOn);

	RunMode getScheduleMode();

	TriggerType getTriggerType();

	String getProcessID();

	ScheduleType getScheduleType();

	Calendar getStartTime();

	ArrayList<DaysOfWeek> getDaysOfWeek();

	void setNextStartTime(Calendar time);

	int getMinuteInterval();

	boolean scriptIsValid();

}
