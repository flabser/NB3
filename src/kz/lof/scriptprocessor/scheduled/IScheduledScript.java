package kz.lof.scriptprocessor.scheduled;

import kz.lof.scripting._Session;
import kz.lof.webserver.servlet.PageOutcome;

public interface IScheduledScript {
	void setSession(_Session ses);

	void setPageOutcome(PageOutcome outcome);

	PageOutcome processCode(String method);

}
