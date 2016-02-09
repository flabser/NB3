package kz.pchelka.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import kz.flabs.util.Util;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.ScheduleType;
import kz.pchelka.log.ILogger;
import kz.lof.server.Server;

public class PeriodicalServices extends TimerTask {
	private ArrayList<IDaemon> activProceses;
	private ArrayList<IDaemon> troubleProceses;
	private Timer timer;
	private ILogger logger;
	private static final int firstStartDelay = 1;
	private static final int maxCurrentTimeMin = 1;

	public PeriodicalServices(ArrayList<IDaemon> activProceses) {
		this.logger = Server.logger;
		this.activProceses = activProceses;
		troubleProceses = new ArrayList<IDaemon>();
	}

	public void start() {
		timer = new java.util.Timer();
		timer.schedule(this, firstStartDelay, Scheduler.minuteInterval * 1);
	}

	@Override
	public void run() {
		Thread currentThread = Thread.currentThread();
		currentThread.setPriority(Thread.MIN_PRIORITY);
		currentThread.setName("NextBase scheduler");

		Calendar currentTime = new GregorianCalendar();
		currentTime.setTime(new Date());
		Object shared = new Object();

		for (IDaemon daemon : activProceses) {
			Calendar startTime = daemon.getStartTime();

			if (daemon.getStatus() != DaemonStatusType.ERROR) {
				if (isWorkingDay(daemon.getDaysOfWeek())) {
					if (daemon.getStatus() != DaemonStatusType.RUNNING) {
						if (daemon.getRule().getScheduleType() == ScheduleType.PERIODICAL) {

							if (currentTime.compareTo(startTime) >= 0) {
								logger.verboseLogEntry("Launch(periodical)>" + daemon.getID());
								daemon.setMonitor(shared);
								Thread t = new Thread(daemon);
								t.setPriority(Thread.MIN_PRIORITY);
								t.setName("Scheduler task:" + daemon.getID());
								t.start();
							}
						} else if (daemon.getRule().getScheduleType() == ScheduleType.INTIME) {
							Calendar maxCurrentTime = (Calendar) startTime.clone();
							maxCurrentTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH),
									currentTime.get(Calendar.DAY_OF_MONTH), startTime.get(Calendar.HOUR_OF_DAY),
									startTime.get(Calendar.MINUTE) + maxCurrentTimeMin);

							if (currentTime.compareTo(startTime) >= 0 && currentTime.before(maxCurrentTime)) {
								logger.verboseLogEntry("Launch(in time)>" + daemon.getID());
								daemon.setMonitor(shared);
								Thread t = new Thread(daemon);
								t.setPriority(Thread.MIN_PRIORITY);
								t.setName("Scheduler task:" + daemon.getID());
								t.start();
							}
						}
					} else {

					}
				}
			} else {
				logger.warningLogEntry("\"" + daemon.getID()
						+ "\" task processing has finished with error, processing by schedule have to end");
				troubleProceses.add(daemon);
			}
		}
		activProceses.removeAll(troubleProceses);
	}

	public String getProcessAsXMLPiece() {
		StringBuffer result = new StringBuffer(1000);
		for (IDaemon process : activProceses) {
			String lst = Util.convertDataTimeToString(process.getLastSuccessTime());
			String nt = Util.convertDataTimeToString(process.getStartTime());
			DaemonStatusType stat = process.getStatus();

			result.append("<entry><id>" + process.getID() + "</id><type>" + process.getDeamonType() + "</type>"
					+ "<lastsuccess>" + lst + "</lastsuccess>" + "<nexttime>" + nt + "</nexttime>" + "<successrun>"
					+ process.getSuccessRunCount() + "</successrun>" + "<runhistory>" + process.getSuccessRunHistory()
					+ "</runhistory>" + "<status>" + stat + "</status></entry>");
		}
		return result.toString();
	}

	private boolean isWorkingDay(ArrayList<DaysOfWeek> days) {
		if (days == null || days.size() == 0) {
			return true;
		}
		if (days.contains(DaysOfWeek.ALL_WEEK)) {
			return true;
		}
		Calendar today = new GregorianCalendar();
		switch (today.get(Calendar.DAY_OF_WEEK)) {
		case 1:
			if (days.contains(DaysOfWeek.SUNDAY)) {
				return true;
			}
		case 2:
			if (days.contains(DaysOfWeek.MONDAY) || days.contains(DaysOfWeek.WORKWEEK)) {
				return true;
			}
		case 3:
			if (days.contains(DaysOfWeek.TUESDAY) || days.contains(DaysOfWeek.WORKWEEK)) {
				return true;
			}
		case 4:
			if (days.contains(DaysOfWeek.WEDNESDAY) || days.contains(DaysOfWeek.WORKWEEK)) {
				return true;
			}
		case 5:
			if (days.contains(DaysOfWeek.THURSDAY) || days.contains(DaysOfWeek.WORKWEEK)) {
				return true;
			}
		case 6:
			if (days.contains(DaysOfWeek.FRIDAY) || days.contains(DaysOfWeek.WORKWEEK)) {
				return true;
			}
		case 7:
			if (days.contains(DaysOfWeek.SATURDAY)) {
				return true;
			}
		}
		return false;
	}

	public void cancelSchedule() {
		logger.normalLogEntry("cancelling scheduler");
		timer.cancel();
	}

	public ArrayList<IDaemon> getCurrentTasks() {
		ArrayList<IDaemon> allProceses = new ArrayList<IDaemon>();
		allProceses.addAll(activProceses);
		allProceses.addAll(troubleProceses);
		return allProceses;
	}

}
