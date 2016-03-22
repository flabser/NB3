package kz.lof.dataengine.jpadatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.lof.appenv.AppEnv;
import kz.lof.env.Environment;

public class DatabaseDeployer implements IDatabaseDeployer {
	public boolean deployed;

	private IDBConnectionPool dbPool;

	public DatabaseDeployer(AppEnv env) throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {
		dbPool = Environment.dataBase.getConnectionPool();
	}

	@Override
	public boolean deploy() {
		try {
			checkAndCreateTable(DDEScripts.getCountersTableDDE(), "COUNTERS");
			String dbVersion = dbPool.getDatabaseVersion();
			dbVersion = dbVersion.substring(dbVersion.indexOf(" ") + 1, dbVersion.indexOf(","));
			deployed = true;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		}
		return deployed;
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

	public boolean createTrigger(String triggerDDE) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.execute(triggerDDE);
			s.close();
			conn.commit();
			return true;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public boolean createIndex(String indexDDE) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.execute(indexDDE);
			s.close();
			conn.commit();
			return true;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public boolean hasTable(String tableName) throws SQLException {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			String query = "select * from " + tableName;
			s.executeQuery(query);
			s.close();
		} catch (Throwable e) {
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return true;
	}

}
