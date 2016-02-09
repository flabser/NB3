package kz.pchelka.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.servlets.admin.IAdministartorForm;
import kz.flabs.util.Util;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.IScheduledProcessRule;
import kz.flabs.webrule.scheduler.ScheduleType;
import kz.lof.server.Server;

public abstract class AbstractDaemon implements IDaemon, Runnable, IProcessInitiator, IAdministartorForm {
	public ArrayList<DaysOfWeek> daysOfWeek = new ArrayList<DaysOfWeek>();

	protected int successRun;
	protected Calendar lastSuccess;
	protected ArrayList<String> runHistory = new ArrayList<String>();
	protected IScheduledProcessRule rule;
	protected IProcessInitiator processOwner;
	protected boolean isFirstStart;

	private DaemonStatusType status = DaemonStatusType.WAIT_FOR_RUN;

	@Override
	abstract public int process(IProcessInitiator processOwner)
			throws DocumentAccessException, RuleException, QueryFormulaParserException, QueryException;

	@Override
	public void init(IScheduledProcessRule rule) {
		this.rule = rule;
		processOwner = rule.getOwner();
		isFirstStart = true;
	}

	@Override
	public String getID() {
		return processOwner.getOwnerID() + "_" + rule.getProcessID();
	}

	@Override
	public TriggerType getTriggerType() {
		return rule.getTriggerType();
	}

	public static void log(String logText) {
		ScriptProcessor.logger.normalLogEntry(logText);
	}

	@Override
	public void run() {
		try {
			setStatus(DaemonStatusType.RUNNING);
			if (process(processOwner) == 0) {
				setStatus(DaemonStatusType.IDLE);
				Calendar finishTime = new GregorianCalendar();
				finishTime.setTime(new Date());
				postSuccess(finishTime);
			} else {
				setStatus(DaemonStatusType.ERROR);
				Server.logger.warningLogEntry("Background process " + getID() + ", has completed with error");
			}
		} catch (Exception e) {
			setStatus(DaemonStatusType.ERROR);
			Server.logger.warningLogEntry("Background process " + getID() + ", has completed with error");
			Server.logger.errorLogEntry(e);
		}

	}

	@Override
	public DaemonType getDeamonType() {
		return rule.getDaemonType();
	}

	@Override
	public void setRule(IScheduledProcessRule rule) {
		this.rule = rule;
	}

	@Override
	public void setMonitor(Object o) {
	}

	@Override
	public void postSuccess(Calendar finishTime) {
		lastSuccess = (Calendar) finishTime.clone();
		if (rule.getScheduleType() == ScheduleType.INTIME) {
			Calendar currentTime = new GregorianCalendar();
			currentTime.setTime(new Date());
			Calendar ruleTime = new GregorianCalendar();
			ruleTime.setTime(rule.getStartTime().getTime());
			finishTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH),
					currentTime.get(Calendar.DAY_OF_MONTH) + 1, ruleTime.get(Calendar.HOUR_OF_DAY),
					ruleTime.get(Calendar.MINUTE));
		} else if (rule.getScheduleType() == ScheduleType.PERIODICAL) {
			finishTime.set(finishTime.get(Calendar.YEAR), finishTime.get(Calendar.MONTH),
					finishTime.get(Calendar.DAY_OF_MONTH), finishTime.get(Calendar.HOUR_OF_DAY),
					finishTime.get(Calendar.MINUTE) + rule.getMinuteInterval());
		}
		Server.logger.normalLogEntry(getID() + "\" has been success finished. Next start "
				+ Util.dateTimeFormat.format(finishTime.getTime()));
		runHistory.add(Util.convertDataTimeToString(finishTime));
		rule.setNextStartTime(finishTime);
		successRun++;

	}

	@Override
	public void setStatus(DaemonStatusType status) {
		this.status = status;

	}

	@Override
	public DaemonStatusType getStatus() {
		return status;
	}

	@Override
	public Calendar getLastSuccessTime() {
		return lastSuccess;
	}

	@Override
	public Calendar getStartTime() {
		return rule.getStartTime();
	}

	@Override
	public IScheduledProcessRule getRule() {
		return rule;
	}

	@Override
	public int getSuccessRunCount() {
		return successRun;
	}

	@Override
	public ArrayList<String> getSuccessRunHistory() {
		return runHistory;
	}

	@Override
	public ArrayList<DaysOfWeek> getDaysOfWeek() {
		return rule.getDaysOfWeek();
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getName();
	}

	@Override
	public String toXML() {
		String lst = Util.convertDataTimeToString(getLastSuccessTime());
		String nt = Util.convertDataTimeToString(getStartTime());
		DaemonStatusType stat = getStatus();
		String xmlFragment = "<id>" + getID() + "</id>" + "<type>" + getDeamonType() + "</type>" + "<trigger>"
				+ getTriggerType() + "</trigger>" + "<lastsuccess>" + lst + "</lastsuccess>" + "<nexttime>" + nt
				+ "</nexttime>" + "<successrun>" + getSuccessRunCount() + "</successrun>" + "<runhistory>"
				+ getSuccessRunHistory() + "</runhistory>" + "<status>" + stat + "</status>";

		return xmlFragment;
	}
}
