package kz.lof.dataengine.jpadatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpadatabase.ftengine.FTSearchEngineDeployer;

public class DatabaseDeployer implements IDatabaseDeployer {
	public boolean deployed;
	private IDBConnectionPool dbPool;

	public DatabaseDeployer(IDatabase db) throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {
		dbPool = db.getConnectionPool();
	}

	@Override
	public boolean deploy() {
		try {
			checkAndCreateTable(DDEScripts.getCountersTableDDE(), "COUNTERS");
			FTSearchEngineDeployer ftEngine = new FTSearchEngineDeployer(dbPool);
			ftEngine.deploy();
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
