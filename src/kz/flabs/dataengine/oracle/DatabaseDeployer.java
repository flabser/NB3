package kz.flabs.dataengine.oracle;

import java.sql.Connection;
import java.sql.Statement;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.dataengine.h2.DBConnectionPool;
import kz.pchelka.scheduler.IProcessInitiator;

public class DatabaseDeployer implements IDatabaseDeployer, IProcessInitiator {
	public boolean deployed;

	private AppEnv env;
	private IDBConnectionPool dbPool;
	private String connectionURL = "";

	public DatabaseDeployer(AppEnv env)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {
		this.env = env;
		connectionURL = env.globalSetting.dbURL;
		dbPool = new DBConnectionPool();
		dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(),
				env.globalSetting.getDbPassword());

	}

	@Override
	public boolean deploy() {
		try {

			checkAndCreateTable(DDEScripts.getDBVersionTableDDE(), "DBVERSION");
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		}
		return deployed;
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean patch() {
		return false;
	}

	public boolean checkAndCreateTable(String scriptCreateTable, String tableName) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			if (!DatabaseUtil.hasTable(tableName, conn)) {
				if (!s.execute(scriptCreateTable)) {
					conn.commit();
					s.close();
					return true;
				} else {
					AppEnv.logger.errorLogEntry("error 72169");
				}
			}
			s.close();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}
}
