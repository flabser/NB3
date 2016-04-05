package kz.lof.scriptprocessor.scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HourScheduledTask extends ScheduledTask implements Job {

	public HourScheduledTask() {
		schema = ScheduleSchema.EVERY_1_HOUR;
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
