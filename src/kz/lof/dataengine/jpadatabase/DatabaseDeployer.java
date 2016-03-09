package kz.lof.dataengine.jpadatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.dataengine.postgresql.useractivity.UsersActivityDDEScripts;
import kz.lof.appenv.AppEnv;
import kz.lof.env.Environment;

public class DatabaseDeployer implements IDatabaseDeployer {
	public boolean deployed;

	private AppEnv env;
	private IDBConnectionPool dbPool;
	private String connectionURL = "";

	public DatabaseDeployer(AppEnv env) throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {
		this.env = env;
		dbPool = Environment.dataBase.getConnectionPool();

	}

	@Override
	public boolean deploy() {
		try {
			checkAndCreateTable(DDEScripts.getDBVersionTableDDE(), "DBVERSION");
			checkAndCreateTable(DDEScripts.getCountersTableDDE(), "COUNTERS");
			checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityDDE(), "USERS_ACTIVITY");
			checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityChangesDDE(), "USERS_ACTIVITY_CHANGES");
			checkAndCreateTable(UsersActivityDDEScripts.getActivityDDE(), "ACTIVITY");
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

	public boolean checkAndCreateTrigger(String mainTableName) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.addBatch("DROP TRIGGER IF EXISTS set_remove_att_flag_" + mainTableName + " ON CUSTOM_BLOBS_" + mainTableName);
			s.addBatch(DDEScripts.getAttachmentTriggerDDE(mainTableName));
			s.executeBatch();
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

	public boolean checkAndCreateNamedTrigger(String scriptCreateTrigger, String triggerName, String mainTableName) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.addBatch("DROP TRIGGER IF EXISTS " + triggerName + " ON " + mainTableName);
			s.addBatch(scriptCreateTrigger);
			s.executeBatch();
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

	public boolean checkAndCreateResponseTrigger(String mainTableName) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.addBatch("DROP TRIGGER IF EXISTS set_remove_resp_flag_" + mainTableName + " ON " + mainTableName);
			s.addBatch(DDEScripts.getResponsesTriggerDDE(mainTableName));
			s.executeBatch();
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

	// fts BEFORE INSERT OR UPDATE trigger
	public boolean checkAndCreateTriggerFTS(String mainTableName) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.addBatch("DROP TRIGGER IF EXISTS trigger_update_" + mainTableName + "_fts ON " + mainTableName + ";");
			s.addBatch("CREATE TRIGGER trigger_update_" + mainTableName + "_fts " + " BEFORE INSERT OR UPDATE ON " + mainTableName
			        + " FOR EACH ROW EXECUTE PROCEDURE update_fts_tsvector();");
			s.executeBatch();
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

	public boolean executeQuery(String query) {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.execute(query);
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

	public void checkAndCreateProsedure(String scriptCreateTable, String procedurName) {
		Connection conn = dbPool.getConnection();
		try {
			if (!DatabaseUtil.hasProcedureAndTriger(procedurName, conn)) {
				PreparedStatement pst = conn.prepareStatement(scriptCreateTable);
				pst.execute();
				pst.close();
			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
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

	public boolean checkAndCreateIndex(String tableName, String indexName) {
		Connection conn = dbPool.getConnection();
		try {
			DatabaseMetaData metadata = conn.getMetaData();
			ResultSet rs = metadata.getIndexInfo(null, "public", tableName, false, false);
			while (rs.next()) {
				if (rs.getString("INDEX_NAME").equalsIgnoreCase(indexName)) {
					return true;
				}
			}
			createTrigger("create index " + indexName + " on " + tableName + " using gin (fts)");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return true;
	}

	@Override
	public boolean patch() {

		return false;
	}

}
