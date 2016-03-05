package kz.flabs.runtimeobj.page;

import kz.flabs.dataengine.Const;
import kz.flabs.users.UserSession;
import kz.lof.appenv.AppEnv;
import kz.lof.rule.page.PageRule;
import kz.lof.scripting._Session;

public class IncludedPage extends Page implements Const {

	@Deprecated
	public IncludedPage(AppEnv env, UserSession userSession, PageRule rule) {
		super(env, userSession, rule);
	}

	public IncludedPage(AppEnv env, _Session ses, PageRule rule) {
		super(env, ses, rule);
	}

	@Override
	public String getCacheID() {
		return "INCLUDED_PAGE_" + env.appType + "_" + rule.id + "_" + userSession.lang;

	}

}
