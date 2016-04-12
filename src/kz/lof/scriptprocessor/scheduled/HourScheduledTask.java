package kz.lof.scriptprocessor.scheduled;

import org.quartz.Job;

public class HourScheduledTask extends ScheduledTask implements Job {

	public HourScheduledTask() {
		schema = ScheduleSchema.EVERY_1_HOUR;
	}

}
