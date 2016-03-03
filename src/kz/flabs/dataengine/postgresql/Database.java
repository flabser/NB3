package kz.flabs.dataengine.postgresql;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.IDatabase;
import kz.lof.appenv.AppEnv;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		// super(env, true);
		databaseType = DatabaseType.POSTGRESQL;
	}

}
