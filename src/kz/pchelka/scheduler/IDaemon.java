package kz.pchelka.scheduler;

import java.util.ArrayList;
import java.util.Calendar;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.IScheduledProcessRule;


public interface IDaemon extends Runnable{
	String getID();
	void init(IScheduledProcessRule rule);
	DaemonType getDeamonType();
	int process(IProcessInitiator env) throws DocumentAccessException, RuleException, QueryFormulaParserException, QueryException;
	void setRule(IScheduledProcessRule rule);
	void setMonitor(Object o);
	void postSuccess(Calendar finishTime);
	void setStatus(DaemonStatusType status);
	DaemonStatusType getStatus();
	Calendar getLastSuccessTime();
	Calendar getStartTime();
	IScheduledProcessRule getRule();
	int getSuccessRunCount();
	ArrayList<String> getSuccessRunHistory();
	ArrayList<DaysOfWeek> getDaysOfWeek();
	TriggerType getTriggerType();
	
}
