package kz.lof.scripting.event;

import kz.lof.scripting._Session;
import kz.lof.scriptprocessor.scheduled.AbstractScheduledTask;

public abstract class _DoScheduledTask extends AbstractScheduledTask {

	@Override
	public abstract void doEvery5Min(_Session session);

	@Override
	public abstract void doEvery1Hour(_Session session);

	@Override
	public abstract void doEveryNight(_Session session);

}
