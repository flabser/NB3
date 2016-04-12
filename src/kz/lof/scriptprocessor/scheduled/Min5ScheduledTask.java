package kz.lof.scriptprocessor.scheduled;

import org.quartz.Job;

public class Min5ScheduledTask extends ScheduledTask implements Job {

	public Min5ScheduledTask() {
		schema = ScheduleSchema.EVERY_5_MIN;
	}

}
