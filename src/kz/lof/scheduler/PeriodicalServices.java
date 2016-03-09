package kz.lof.scheduler;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;

import java.util.Date;
import java.util.List;

import kz.lof.scheduler.tasks.TempFileCleaner;
import kz.lof.scriptprocessor.scheduled.HourScheduledTask;
import kz.lof.scriptprocessor.scheduled.Min5ScheduledTask;
import kz.lof.scriptprocessor.scheduled.NightScheduledTask;
import kz.lof.server.Server;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class PeriodicalServices {
	private Scheduler sched;

	public PeriodicalServices() {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			sched = sf.getScheduler();
			Date runTime = evenMinuteDate(new Date());

			JobDetail fileCleanerJob = newJob(TempFileCleaner.class).withIdentity("file_cleaner", "system").build();
			JobDetail logsZipJob = newJob(TempFileCleaner.class).withIdentity("logs_zip", "system").build();

			JobDetail min5Job = newJob(Min5ScheduledTask.class).withIdentity("5_min_task", "application").build();
			JobDetail hourJob = newJob(HourScheduledTask.class).withIdentity("1_hours_task", "application").build();
			JobDetail nightJob = newJob(NightScheduledTask.class).withIdentity("night_task", "application").build();

			Trigger triggerFileCleaner = TriggerBuilder.newTrigger().withIdentity("system_5_min_trigger", "system")
			        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(5).repeatForever()).startAt(runTime).build();
			Trigger triggerLogsZip = TriggerBuilder.newTrigger().withIdentity("system_night_trigger", "system")
			        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24).repeatForever()).startAt(runTime).build();

			Trigger trigger5min = TriggerBuilder.newTrigger().withIdentity("5_min_trigger", "application")
			        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(5).repeatForever()).startAt(runTime).build();
			Trigger triggerHour = TriggerBuilder.newTrigger().withIdentity("hour_trigger", "application")
			        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(1).repeatForever()).startAt(runTime).build();
			Trigger triggerNight = TriggerBuilder.newTrigger().withIdentity("night_trigger", "application")
			        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24).repeatForever()).startAt(runTime).build();

			sched.scheduleJob(fileCleanerJob, triggerFileCleaner);
			sched.scheduleJob(logsZipJob, triggerLogsZip);
			sched.scheduleJob(min5Job, trigger5min);
			sched.scheduleJob(hourJob, triggerHour);
			sched.scheduleJob(nightJob, triggerNight);
			sched.start();

		} catch (SchedulerException e) {
			Server.logger.errorLogEntry(e);
		}
	}

	public String getCurrentJobs() {
		String result = "";

		try {
			List<JobExecutionContext> jobs = sched.getCurrentlyExecutingJobs();

			for (JobExecutionContext j : jobs) {
				result = result + j.toString();
			}
			return result;
		} catch (SchedulerException e) {
			Server.logger.errorLogEntry(e);
		}
		return result;
	}

	public void stop() {
		try {
			sched.shutdown(true);
		} catch (SchedulerException e) {
			Server.logger.errorLogEntry(e);
		}
	}
}
