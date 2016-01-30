package kz.flabs.dataengine.oracle;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.IDatabase;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {

	public Database(AppEnv env)
			throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(env, true);
		databaseType = DatabaseType.ORACLE;
	}

}
