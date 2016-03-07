package kz.lof.scriptprocessor.scheduled;

import kz.lof.scripting._Session;

public abstract class AbstractPage implements IScheduledScript {

	public abstract void doEvery5Min(_Session session);

	public abstract void doEvery1Hour(_Session session);

	public abstract void doEveryDay(_Session session);

}
