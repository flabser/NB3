package kz.lof.scriptprocessor.scheduled;

import kz.lof.scripting._Session;

public interface IScheduledScript {
	void setSession(_Session ses);

	void setOutcome(ScheduledTaskOutcome outcome);

	ScheduledTaskOutcome processCode(ScheduleSchema schema);

}
