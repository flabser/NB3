package kz.lof.scriptprocessor.scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NightScheduledTask extends ScheduledTask implements Job {

	public NightScheduledTask() {
		schema = ScheduleSchema.EVERY_NIGHT;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			processCode("staff.scheduled.BirtdayReminder");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
