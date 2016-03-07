package kz.flabs.runtimeobj.page;

import kz.flabs.dataengine.Const;
import kz.lof.appenv.AppEnv;
import kz.lof.rule.page.PageRule;
import kz.lof.scripting._Session;

public class IncludedPage extends Page implements Const {

	public IncludedPage(AppEnv env, _Session ses, PageRule rule) {
		super(env, ses, rule);
	}

	@Override
	public String getCacheID() {
		return "INCLUDED_PAGE_" + env.appName + "_" + rule.id + "_" + ses.getLang();

	}

}
