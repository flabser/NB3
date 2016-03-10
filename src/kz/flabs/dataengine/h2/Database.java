package kz.flabs.dataengine.h2;

import java.text.SimpleDateFormat;

import javax.persistence.EntityManagerFactory;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.IActivity;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.dataengine.IUsersActivity;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.rule.RuleProvider;
import kz.lof.server.Server;
import kz.pchelka.log.ILogger;

public class Database extends DatabaseCore implements IDatabase, Const {
	public boolean isValid;
	public RuleProvider ruleProvider;
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static final SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static ILogger logger = Server.logger;

	// TODO need to improve to make it more secure
	protected static String dbUser = "ss";
	protected static String dbPwd = "maza";
	protected static String connectionURL = "";
	protected IDBConnectionPool dbPool;
	protected IDBConnectionPool structDbPool;
	// protected IDBConnectionPool forumDbPool;
	protected String dbID;
	protected AppEnv env;
	protected IUsersActivity usersActivity;
	protected IActivity activity;
	protected static String baseTable = "MAINDOCS";

	protected String externalStructureApp;

	public Database() {

	}

	public Database(AppEnv env, DatabaseType dbType) throws DatabasePoolException, InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		this.env = env;

		// dbID = env.globalSetting.databaseName;
		// connectionURL = env.globalSetting.dbURL;
		dbPool = new DBConnectionPool();
		dbPool.initConnectionPool(EnvConst.JDBC_DRIVER, connectionURL, dbUser, dbPwd);

		databaseType = dbType;

	}

	protected void initStructPool() {
		/*
		 * for (ExternalModule module : env.globalSetting.extModuleMap.values())
		 * { if (module.getType() == ExternalModuleType.STRUCTURE) {
		 * externalStructureApp = module.getName();
		 * Environment.addDelayedInit(this); } else {
		 * Environment.addDelayedInit(this); } }
		 */
		structDbPool = dbPool;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public IDBConnectionPool getConnectionPool() {
		return dbPool;
	}

	@Override
	public AppEnv getParent() {
		return null;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return null;
	}

	@Override
	public IDatabase getBaseObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		// TODO Auto-generated method stub
		return null;
	}

}