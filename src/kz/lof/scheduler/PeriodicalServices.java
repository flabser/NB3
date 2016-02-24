package kz.lof.scheduler;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.List;

import kz.lof.scheduler.tasks.TempFileCleaner;
import kz.lof.server.Server;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class PeriodicalServices {
	private Scheduler sched;

	public PeriodicalServices() {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			sched = sf.getScheduler();
			Date runTime = evenMinuteDate(new Date());

			JobDetail job1 = newJob(TempFileCleaner.class).withIdentity("job1", "group1").build();

			Trigger trigger1 = newTrigger().withIdentity("trigger1", "group1").startAt(runTime).build();

			sched.scheduleJob(job1, trigger1);
			// Server.logger.normalLogEntry(job1.getKey() + " will run at: " +
			// runTime);

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
