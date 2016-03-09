package kz.lof.scriptprocessor.scheduled;

import kz.lof.scripting._Session;
import kz.lof.scriptprocessor.ScriptHelper;

public abstract class AbstractScheduledTask extends ScriptHelper implements IScheduledScript {
	private _Session ses;
	private ScheduledTaskOutcome outcome;

	@Override
	public void setSession(_Session ses) {
		this.ses = ses;

	}

	@Override
	public void setOutcome(ScheduledTaskOutcome outcome) {
		this.outcome = outcome;

	}

	@Override
	public ScheduledTaskOutcome processCode(ScheduleSchema schema) {
		switch (schema) {
		case EVERY_5_MIN:
			doEvery5Min(ses);
			break;
		case EVERY_1_HOUR:
			doEvery1Hour(ses);
			break;
		case EVERY_NIGHT:
			doEveryNight(ses);
			break;

		}
		return outcome;
	}

	public abstract void doEvery5Min(_Session session);

	public abstract void doEvery1Hour(_Session session);

	public abstract void doEveryNight(_Session session);

}
