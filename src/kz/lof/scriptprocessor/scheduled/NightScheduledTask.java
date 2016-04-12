package kz.lof.scriptprocessor.scheduled;

import org.quartz.Job;

public class NightScheduledTask extends ScheduledTask implements Job {

	public NightScheduledTask() {
		schema = ScheduleSchema.EVERY_NIGHT;
	}

}
