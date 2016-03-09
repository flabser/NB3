package kz.lof.scriptprocessor.scheduled;

import kz.lof.scripting._Session;
import kz.lof.server.Server;

public class ScheduledTask {
	protected ScheduleSchema schema;
	private ScheduledTaskOutcome outcome;
	private _Session ses;

	public ScheduledTaskOutcome processCode(String className) throws ClassNotFoundException {
		Object object = null;
		try {
			Class<?> pageClass = Class.forName(className);
			object = pageClass.newInstance();
		} catch (InstantiationException e) {
			Server.logger.errorLogEntry(e);
		} catch (IllegalAccessException e) {
			Server.logger.errorLogEntry(e);
		}

		IScheduledScript myObject = (IScheduledScript) object;

		myObject.setOutcome(outcome);
		myObject.setSession(ses);

		return myObject.processCode(schema);
	}

}
