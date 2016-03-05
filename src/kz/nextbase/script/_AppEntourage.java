package kz.nextbase.script;

import kz.lof.appenv.AppEnv;
import kz.lof.scripting._Session;
import kz.lof.server.Server;

//import kz.nextbase.script.struct._Employer;

public class _AppEntourage {
	private AppEnv env;
	private _Session ses;

	public _AppEntourage(_Session ses, AppEnv env) {
		this.ses = ses;
		this.env = env;
	}

	public String getServerVersion() {
		return Server.serverVersion;
	}

	public String getBuildTime() {
		return Server.compilationTime;
	}

	public String getGeneralName() {
		return env.globalSetting.orgName;
	}

	public String getAppName() {
		return env.appType;
	}

}
