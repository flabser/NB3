package kz.lof.dataengine.jpadatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpadatabase.ftengine.FTSearchEngine;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.server.Server;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.postgresql.util.PSQLException;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {
	private Properties props = new Properties();
	protected EntityManagerFactory factory;
	private FTSearchEngine ftEngine;

	public Database() {
		props.setProperty("user", EnvConst.DB_USER);
		props.setProperty("password", EnvConst.DB_PWD);
		String dbURL = "jdbc:postgresql://" + EnvConst.DATABASE_HOST + ":" + EnvConst.CONN_PORT + "/" + Environment.appServerName;
		try {
			if (!hasDatabase(Environment.appServerName)) {
				String dbUser = "ss";
				createDatabase(Environment.appServerName, dbUser);
			}
		} catch (SQLException e) {
			Server.logger.errorLogEntry(e);
		}
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, EnvConst.JDBC_DRIVER);
		properties.put(PersistenceUnitProperties.JDBC_USER, env.globalSetting.getDbUserName());
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, env.globalSetting.getDbPassword());
		properties.put(PersistenceUnitProperties.JDBC_URL, dbURL);

		// INFO,
		// OFF,
		// ALL,
		// CONFIG (developing)
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "OFF");
		properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
		properties
		        .put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION);

		PersistenceProvider pp = new PersistenceProvider();
		factory = pp.createEntityManagerFactory(env.appType, properties);
		if (factory == null) {
			Server.logger.errorLogEntry("the entity manager of \"" + env.appType + "\" has not been initialized");

		}
		ftEngine = new FTSearchEngine(this);
	}

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(env, DatabaseType.JPA);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, EnvConst.JDBC_DRIVER);
		properties.put(PersistenceUnitProperties.JDBC_USER, env.globalSetting.getDbUserName());
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, env.globalSetting.getDbPassword());
		properties.put(PersistenceUnitProperties.JDBC_URL, connectionURL);

		// INFO,
		// OFF,
		// ALL,
		// CONFIG (developing)
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "OFF");
		properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
		properties
		        .put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION);

		PersistenceProvider pp = new PersistenceProvider();
		factory = pp.createEntityManagerFactory(env.appType, properties);
		if (factory == null) {
			Server.logger.errorLogEntry("the entity manager of \"" + env.appType + "\" has not been initialized");

		}
		ftEngine = new FTSearchEngine(this);
	}

	public int createDatabase(String name, String dbUser) throws SQLException {
		if (!hasDatabase(name)) {
			Connection conn = DriverManager.getConnection(Environment.getDbURL());
			try {
				Statement st = conn.createStatement();
				String sql = "CREATE DATABASE \"" + name + "\" WITH OWNER = " + dbUser + " ENCODING = 'UTF8'";
				st.executeUpdate(sql);
				st.executeUpdate("GRANT ALL privileges ON DATABASE \"" + name + "\" TO " + dbUser);
				st.close();
				return 0;
			} catch (Throwable e) {
				DatabaseUtil.debugErrorPrint(e);
				return -1;
			}
		} else {
			return 1;
		}
	}

	@Override
	protected void initStructPool() {
		for (ExternalModule module : env.globalSetting.extModuleMap.values()) {
			if (module.getType() == ExternalModuleType.STAFF) {
				externalStructureApp = module.getName();
				Environment.addDelayedInit(this);
			} else {
				Environment.addDelayedInit(this);
			}
		}
		structDbPool = dbPool;
	}

	@Override
	public String toString() {
		return "version JPA";
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		return ftEngine;
	}

	private boolean hasDatabase(String dbName) throws SQLException {
		Connection conn = DriverManager.getConnection(Environment.getDbURL());
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				return true;
			}
			s.close();
			conn.commit();
			return false;
		} catch (Throwable e) {
			return false;
		}
	}

	public int registerUser(String dbUser, String dbPwd) throws SQLException {

		Connection conn = DriverManager.getConnection(Environment.getDbURL(), props);
		try {
			Statement st = conn.createStatement();
			try {
				st.executeUpdate("CREATE USER  " + dbUser + " WITH password '" + dbPwd + "'");
				return 0;
			} catch (PSQLException sqle) {
				Server.logger.warningLogEntry("database user \"" + dbUser + "\" already exists");
				return 1;
			} catch (Exception e) {
				Server.logger.errorLogEntry(e.getMessage());
				return 1;
			}
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		}

	}

}
