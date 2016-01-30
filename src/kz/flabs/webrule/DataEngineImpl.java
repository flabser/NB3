package kz.flabs.webrule;

import kz.flabs.dataengine.DatabaseType;
import kz.pchelka.server.Server;

public class DataEngineImpl {
	Class<?> database;
	Class<?> deployer;

	public DataEngineImpl(DatabaseType databaseType) {
		if (databaseType == DatabaseType.POSTGRESQL) {
			deployer = kz.flabs.dataengine.postgresql.DatabaseDeployer.class;
			database = kz.flabs.dataengine.postgresql.Database.class;
		} else if (databaseType == DatabaseType.MSSQL) {
			deployer = kz.flabs.dataengine.mssql.DatabaseDeployer.class;
			database = kz.flabs.dataengine.mssql.Database.class;
		} else if (databaseType == DatabaseType.ORACLE) {
			deployer = kz.flabs.dataengine.oracle.DatabaseDeployer.class;
			database = kz.flabs.dataengine.oracle.Database.class;
		} else {
			deployer = kz.flabs.dataengine.h2.DatabaseDeployer.class;
			database = kz.flabs.dataengine.h2.Database.class;
		}
	}

	public DataEngineImpl(String deployerClass, String databaseClass) {
		try {
			deployer = Class.forName(deployerClass);
			database = Class.forName(databaseClass);
		} catch (ClassNotFoundException e) {
			Server.logger.errorLogEntry(e);
		}
	}

	public Class<?> getDeployerClass() {
		return deployer;
	}

	public Class<?> getDatabaseClass() {
		return database;
	}

}
