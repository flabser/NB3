package kz.lof.scriptprocessor.page;

import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.server.Server;
import kz.lof.webserver.servlet.PageOutcome;

public class DoProcessor {
	private PageOutcome outcome;
	private _Session ses;
	private _WebFormData webFormData;

	public DoProcessor(PageOutcome outcome, _Session ses, _WebFormData webFormData) {
		this.outcome = outcome;
		this.ses = ses;
		this.webFormData = webFormData;
	}

	public PageOutcome processScenario(String className, String method) throws ClassNotFoundException {
		Object object = null;
		try {
			Class<?> pageClass = Class.forName(className);
			object = pageClass.newInstance();
		} catch (InstantiationException e) {
			Server.logger.errorLogEntry(e);
		} catch (IllegalAccessException e) {
			Server.logger.errorLogEntry(e);
		}

		IPageScript myObject = (IPageScript) object;

		myObject.setPageOutcome(outcome);
		myObject.setSession(ses);
		myObject.setFormData(webFormData);

		return myObject.processCode(method);
	}

}
