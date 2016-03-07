package kz.lof.scripting.event;

import kz.lof.scripting._Session;
import kz.lof.scriptprocessor.scheduled.ScheduledTask;

public abstract class _DoScheduledTask extends ScheduledTask {

	public abstract void doEvery5Min(_Session session);

	public abstract void doEvery1Hour(_Session session);

	public abstract void doEveryDay(_Session session);

}
