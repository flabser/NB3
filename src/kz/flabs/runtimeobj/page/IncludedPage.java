package kz.flabs.runtimeobj.page;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.page.PageRule;

public class IncludedPage extends Page implements Const {

	public IncludedPage(AppEnv env, UserSession userSession, PageRule rule) {
		super(env, userSession, rule);
	}

	@Override
	public String getID() {
		return "INCLUDED_PAGE_" + rule.id + "_" + userSession.lang;

	}

}
