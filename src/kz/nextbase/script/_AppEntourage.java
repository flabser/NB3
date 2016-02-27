package kz.nextbase.script;

import java.sql.SQLException;
import java.util.Collection;

import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.User;
import kz.flabs.webrule.Lang;
import kz.flabs.webrule.Skin;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
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

	public String getLogoImg() {
		return env.globalSetting.logo;
	}

	public String getAppName() {
		return env.appType;
	}

	public _ViewEntryCollection getAvailableLangs() throws _Exception {
		ViewEntryCollection vec = new ViewEntryCollection(ses, 100);

		for (Lang l : env.globalSetting.langsList) {
			String p[] = { l.isOn.toString(), l.id, l.name, Boolean.toString(l.isPrimary) };
			try {
				IViewEntry entry = new ViewEntry(p);
				vec.add(entry);
			} catch (SQLException e) {
				throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "internal error: function: _Document.getAvailableLangs()");
			}
		}
		return vec.getScriptingObj();
	}

	public _ViewEntryCollection getAvailableSkins() throws _Exception {
		ViewEntryCollection vec = new ViewEntryCollection(ses, 100);

		for (Skin skin : env.globalSetting.skinsList) {
			String p[] = { skin.isOn.toString(), skin.id, skin.name };
			try {
				IViewEntry entry = new ViewEntry(p);
				vec.add(entry);
			} catch (SQLException e) {
				throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "internal error: function: _Document.getAvailableSkins()");
			}
		}
		return vec.getScriptingObj();
	}

	public _ViewEntryCollection getAvailableApps() throws _Exception {
		ViewEntryCollection vec = new ViewEntryCollection(ses, 100);
		// _Employer emp = ses.getCurrentAppUser();
		User user = ses.getUser();

		for (AppEnv appEnv : Environment.getApplications()) {
			if (appEnv.isValid && !appEnv.globalSetting.isWorkspace) {
				if (user.authorized) {
					Collection<UserApplicationProfile> enabledApps;
					enabledApps = user.enabledApps.values();
					for (UserApplicationProfile uap : enabledApps) {
						if (uap.appName.equals(appEnv.appType) && !appEnv.appType.equalsIgnoreCase(EnvConst.ADMINISTRATOR_APP_NAME)) {

							String p[] = { appEnv.appType, appEnv.globalSetting.defaultRedirectURL, appEnv.globalSetting.logo,
							        appEnv.globalSetting.orgName, appEnv.globalSetting.description };
							try {
								IViewEntry entry = new ViewEntry(p);
								vec.add(entry);
							} catch (SQLException e) {
								throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "internal error: function: _Document.getAvailableApps()");
							}

						}
					}
				}
			}
		}
		return vec.getScriptingObj();
	}
}
