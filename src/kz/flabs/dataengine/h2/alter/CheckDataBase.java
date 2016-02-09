package kz.flabs.dataengine.h2.alter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.h2.DBConnectionPool;
import kz.lof.server.Server;

public class CheckDataBase {

	private DBConnectionPool dbPool;
	private Updates updates;

	public CheckDataBase(AppEnv env, boolean auth) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	        DatabasePoolException {
		dbPool = new DBConnectionPool();
		if (auth) {
			dbPool.initConnectionPool(env.globalSetting.driver, env.globalSetting.dbURL, env.globalSetting.getDbUserName(),
			        env.globalSetting.getDbPassword());
		} else {
			dbPool.initConnectionPool(env.globalSetting.driver, env.globalSetting.dbURL);
		}

		Updates u = new Updates();
		setUpdates(u);
	}

	public void setUpdates(Updates updates) {
		this.updates = updates;
	}

	public boolean check() throws Exception {
		int currentDBVersion = 0;
		boolean needCheck = true;

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			while (needCheck) {
				String sql = "SELECT * FROM DBVERSION";
				PreparedStatement pst = conn.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {
					currentDBVersion = rs.getInt("VERSION");
				} else {

				}
				pst.close();

			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return true;
	}

	private void updateDBVersion(Connection conn1, int oldVersion, int version) throws SQLException {
		Connection conn = dbPool.getConnection();
		conn.setAutoCommit(false);
		try {
			String updatesQL = "update DBVERSION set OLDVERSION = ?, VERSION = ?, UPDATEDATE = ?";
			PreparedStatement pst = conn.prepareStatement(updatesQL);
			pst.setInt(1, oldVersion);
			pst.setInt(2, version);
			pst.setTimestamp(3, getCurrentDate());
			pst.executeUpdate();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		}
		Server.logger.normalLogEntry("Database structure has updated from " + oldVersion + " to version " + version);
	}

	public void addColumn(String column, String type, String tableName) {
		Connection conn = dbPool.getConnection();
		try {
			String sql = "alter table " + tableName + " add " + column + " " + type;
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();
			pst.close();
		} catch (Exception e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.close(conn);
			dbPool.returnConnection(conn);
			conn = null;
		}
	}

	public void removeColumn(String column, String tableName) {
		Connection conn = dbPool.getConnection();
		try {
			String sql = "alter table " + tableName + " drop " + column + " " + tableName;
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();
			pst.close();
		} catch (Exception e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.close(conn);
			dbPool.returnConnection(conn);
			conn = null;
		}
	}

	public void changeLenghtVarchar(String tableName, String columnName, int lenght) {
		Connection conn = dbPool.getConnection();
		try {
			String sql = "alter table " + tableName + " alter " + columnName + " set data type varchar(" + lenght + ")";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.close(conn);
			dbPool.returnConnection(conn);
			conn = null;
		}
	}

	public void getTable() {
		Connection conn = dbPool.getConnection();
		try {
			DatabaseMetaData md = conn.getMetaData();
			System.out.println();
			ResultSet rs = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				// System.out.println(rs.getString("TABLE_NAME"));
			}

			rs = md.getColumns(null, null, "MAINDOCS", null);
			while (rs.next()) {
				System.out.println(rs.getString("COLUMN_NAME"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbPool.close(conn);
			dbPool.returnConnection(conn);
			conn = null;
		}
	}

	/*
	 * private void testTable(){ Connection conn = dbPool.getConnection(); try{
	 * String sql = "create table test(ID int, name varchar(32))";
	 * PreparedStatement pst = conn.prepareStatement(sql); pst.execute();
	 * }catch(SQLException e){ System.out.println(e); } }
	 */

	private static java.sql.Timestamp getCurrentDate() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	}

}