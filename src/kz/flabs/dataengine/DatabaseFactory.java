package kz.flabs.dataengine;

import kz.lof.appenv.AppEnv;
import kz.lof.env.Environment;

public class DatabaseFactory implements Const {

	public static IDatabase getDatabase(String appName) {
		AppEnv appEnvironment = Environment.getApplication(appName);
		return appEnvironment.getDataBase();
	}

}
