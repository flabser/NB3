package kz.lof.scriptprocessor.scheduled;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import kz.lof.scheduler.SchedulerHelper;
import kz.lof.server.Server;

public class ScheduledTask {
	protected ScheduleSchema schema;

	public ScheduledTaskOutcome processCode(IScheduledScript myObject) throws ClassNotFoundException {
		myObject.setOutcome(new ScheduledTaskOutcome());
		return myObject.processCode(schema);
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			SchedulerHelper sh = new SchedulerHelper();
			for (IScheduledScript sc : sh.getAllScheduledTasks(false).values()) {
				processCode(sc);
			}

		} catch (ClassNotFoundException | IOException e) {
			Server.logger.errorLogEntry(e);
		}
	}
}
