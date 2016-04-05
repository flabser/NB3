package kz.lof.scriptprocessor.scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Min5ScheduledTask extends ScheduledTask implements Job {

	public Min5ScheduledTask() {
		schema = ScheduleSchema.EVERY_5_MIN;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			processCode("staff.scheduled.BirtdayReminder");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

}
