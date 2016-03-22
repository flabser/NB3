package kz.lof.dataengine.jpadatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.dataengine.h2.DBConnectionPool;
import kz.lof.administrator.dao.ApplicationDAO;
import kz.lof.administrator.dao.LanguageDAO;
import kz.lof.administrator.init.FillApplications;
import kz.lof.administrator.init.FillLangs;
import kz.lof.administrator.model.Application;
import kz.lof.administrator.model.Language;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpadatabase.ftengine.FTSearchEngine;
import kz.lof.env.EnvConst;
import kz.lof.exception.SecureException;
import kz.lof.scripting._Session;
import kz.lof.server.Console;
import kz.lof.server.Server;
import kz.lof.user.SuperUser;
import kz.lof.util.StringUtil;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.postgresql.util.PSQLException;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {
	protected EntityManagerFactory factory;
	private FTSearchEngine ftEngine;
	private boolean isNascence;

	public Database() {

		Properties props = new Properties();
		props.setProperty("user", EnvConst.DB_USER);
		props.setProperty("password", EnvConst.DB_PWD);
		String sysDbURL = "jdbc:postgresql://" + EnvConst.DATABASE_HOST + ":" + EnvConst.CONN_PORT + "/postgres";

		try {
			if (!hasDatabase(EnvConst.APP_NAME, sysDbURL, props)) {
				Server.logger.infoLogEntry("Creating database \"" + EnvConst.APP_NAME + "\"...");
				registerUser(dbUser, dbPwd, sysDbURL, props);
				if (createDatabase(EnvConst.APP_NAME, dbUser, sysDbURL, props) == 0) {
					isNascence = true;
				}
			}
		} catch (SQLException e) {
			Server.logger.errorLogEntry(e);
		}

		connectionURL = "jdbc:postgresql://" + EnvConst.DATABASE_HOST + ":" + EnvConst.CONN_PORT + "/" + EnvConst.APP_NAME;

		dbPool = new DBConnectionPool();
		try {
			dbPool.initConnectionPool(EnvConst.JDBC_DRIVER, connectionURL, dbUser, dbPwd);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | DatabasePoolException e) {
			e.printStackTrace();
		}

		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, EnvConst.JDBC_DRIVER);
		properties.put(PersistenceUnitProperties.JDBC_USER, dbUser);
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, dbPwd);
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
		factory = pp.createEntityManagerFactory(EnvConst.ADMINISTRATOR_APP_NAME, properties);
		if (factory == null) {
			Server.logger.errorLogEntry("the entity manager of \"" + EnvConst.ADMINISTRATOR_APP_NAME + "\" has not been initialized");
		} else {

			if (isNascence) {
				Server.logger.infoLogEntry("Loading primary data...");
				AppEnv env = new AppEnv(EnvConst.ADMINISTRATOR_APP_NAME);
				env.setDataBase(this);
				_Session ses = new _Session(env, new SuperUser());

				FillLangs fl = new FillLangs();
				List<Language> entities = fl.getData(ses, null, null);
				LanguageDAO dao = new LanguageDAO(ses);
				for (Language entity : entities) {
					try {
						dao.add(entity);
					} catch (DatabaseException | SecureException e) {
						Server.logger.errorLogEntry(e);
					}
				}

				FillApplications fa = new FillApplications();
				List<Application> appEntities = fa.getData(ses, null, null);
				ApplicationDAO aDao = new ApplicationDAO(ses);
				for (Application entity : appEntities) {
					try {
						aDao.add(entity);
					} catch (DatabaseException | SecureException e) {
						Server.logger.errorLogEntry(e);
					}
				}

			}
		}
	}

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(env);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, EnvConst.JDBC_DRIVER);
		properties.put(PersistenceUnitProperties.JDBC_USER, dbUser);
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, dbPwd);
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
		factory = pp.createEntityManagerFactory(env.appName, properties);
		if (factory == null) {
			Server.logger.errorLogEntry("the entity manager of \"" + env.appName + "\" has not been initialized");

		}
		ftEngine = new FTSearchEngine(this);
	}

	public int registerUser(String dbUser, String dbPwd, String dbURL, Properties props) throws SQLException {

		Connection conn = DriverManager.getConnection(dbURL, props);
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

	public int createDatabase(String dbName, String dbUser, String dbURL, Properties prop) throws SQLException {
		if (!hasDatabase(dbName, dbURL, prop)) {
			Connection conn = DriverManager.getConnection(dbURL, prop);
			try {
				Statement st = conn.createStatement();
				String sql = "CREATE DATABASE \"" + dbName + "\" WITH OWNER = " + dbUser + " ENCODING = 'UTF8'";
				st.executeUpdate(sql);
				st.executeUpdate("GRANT ALL privileges ON DATABASE \"" + dbName + "\" TO " + dbUser);
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
	public String toString() {
		return "version NB3";
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		return ftEngine;
	}

	private boolean hasDatabase(String dbName, String dbURL, Properties prop) throws SQLException {
		Connection conn = DriverManager.getConnection(dbURL, prop);
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

	private int theFirst() {
		List<String> lwd = Console.getValFromConsole("enter user name> ", StringUtil.USERNAME_PATTERN);
		if (lwd != null) {
			if (lwd.contains("quit")) {
				Server.shutdown();
			} else {
				String userName = lwd.get(0);
				String pwd = "";
				try {
					pwd = lwd.get(1);
				} catch (IndexOutOfBoundsException e) {
					pwd = userName;
				}

				System.out.println(userName + " " + pwd);

				/*
				 * User entity = new User(); entity.setLogin(userName);
				 * entity.setPwd(pwd); UserDAO uDao = new UserDAO(this);
				 * uDao.add(entity);
				 */

			}
		}
		return 0;

	}

}
