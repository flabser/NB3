package kz.flabs.dataengine.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.Properties;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabasePoolExceptionType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.lof.server.Server;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class DBConnectionPool implements IDBConnectionPool {
	protected GenericObjectPool connectionPool;
	protected static int timeBetweenEvictionRunsMillis = 1000 * 60 * 15;

	private boolean isValid;
	private String DBMSVersion = "";

	@Override
	public void initConnectionPool(String driver, String dbURL, String userName, String password) throws DatabasePoolException,
	        InstantiationException, IllegalAccessException, ClassNotFoundException {
		Properties props = null;
		Class.forName(driver).newInstance();
		connectionPool = new GenericObjectPool(null);
		connectionPool.setTestOnBorrow(true);
		connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
		connectionPool.setMaxWait(15000);
		connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

		props = new Properties();
		props.setProperty("user", userName);
		props.setProperty("password", password);
		props.setProperty("accessToUnderlyingConnectionAllowed", "true");

		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbURL, props);
		String validationQuery = "SELECT 1";

		new PoolableConnectionFactory(connectionFactory, connectionPool, null, validationQuery, false, true);
		new PoolingDataSource(connectionPool);
		connectionPool.setMaxIdle(200);
		connectionPool.setMaxActive(2000);

		checkConnection();
		isValid = true;
	}

	@Override
	public void initConnectionPool(String driver, String dbURL) throws DatabasePoolException, InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		Class.forName(driver).newInstance();
		connectionPool = new GenericObjectPool(null);
		connectionPool.setTestOnBorrow(true);
		connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
		connectionPool.setMaxWait(15000);
		connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbURL, null, null);
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1", false, true);
		new PoolingDataSource(connectionPool);
		connectionPool.setMaxIdle(200);
		connectionPool.setMaxActive(2000);

		// checkConnection();
		isValid = true;
	}

	@Override
	public Connection getConnection() {
		Connection con = null;
		try {
			con = (Connection) connectionPool.borrowObject();
			con.setAutoCommit(false);
		} catch (NoSuchElementException nsee) {
			Server.logger.errorLogEntry(nsee);
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	@Override
	public void returnConnection(Connection con) {
		if (con == null) {
			// System.err.println("Returning NULL to pool!!!");
			return;
		}

		try {
			connectionPool.returnObject(con);
		} catch (Exception ex) {
			ex.fillInStackTrace();
		}
	}

	@Override
	public void close(Connection conn) {
		/*
		 * if (null != conn) { try { // conn.close(); // } catch (SQLException
		 * e) { //e.printStackTrace(); }
		 * 
		 * }
		 */
	}

	@Override
	public String getDatabaseVersion() {
		return DBMSVersion;
	}

	@Override
	public void closeAll() {
		connectionPool.clear();
		try {
			connectionPool.close();
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

	@Override
	public int getNumActive() {
		return connectionPool.getNumActive();
	}

	@Override
	public String toXML() {
		return "<active>" + connectionPool.getNumActive() + "</active>" + "<idle>" + connectionPool.getNumIdle() + "</idle>" + "<maxactive>"
		        + connectionPool.getMaxActive() + "</maxactive>" + "<maxidle>" + connectionPool.getMaxIdle() + "</maxidle>";
	}

	private void checkConnection() throws DatabasePoolException {
		Connection con = null;
		try {
			con = (Connection) connectionPool.borrowObject();
			con.setAutoCommit(false);
			Statement st = con.createStatement();
			String checkVersionQuery = "";

			checkVersionQuery = "SELECT value\n" + "FROM information_schema.settings\n" + "WHERE name = 'info.VERSION'";

			checkVersionQuery = "SELECT VERSION()";

			ResultSet rs = st.executeQuery(checkVersionQuery);
			if (rs.next()) {
				DBMSVersion = rs.getString(1);
			}
			con.commit();
			rs.close();
			st.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(e);
			if (e.getMessage().contains("Connection refused")) {
				throw new DatabasePoolException(DatabasePoolExceptionType.DATABASE_CONNECTION_REFUSED);
			} else if (e.getMessage().contains("password authentication failed")) {
				throw new DatabasePoolException(DatabasePoolExceptionType.DATABASE_AUTHETICATION_FAILED);
			} else {
				throw new DatabasePoolException(DatabasePoolExceptionType.DATABASE_SQL_ERROR);
			}
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		} finally {
			returnConnection(con);
		}
	}
	// @TODO removeAbandoned=«true» removeAbandonedTimeout=«30»
	// logAbandoned=«true»
}
