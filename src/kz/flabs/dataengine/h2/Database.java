package kz.flabs.dataengine.h2;

import java.text.SimpleDateFormat;

import javax.persistence.EntityManagerFactory;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IActivity;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.dataengine.IStructure;
import kz.flabs.dataengine.IUsersActivity;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.pchelka.env.Environment;
import kz.pchelka.log.ILogger;
import kz.lof.server.Server;

public class Database extends DatabaseCore implements IDatabase, Const {
	public boolean isValid;
	public WebRuleProvider ruleProvider;
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static final SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static ILogger logger = Server.logger;

	protected String connectionURL = "";
	protected IDBConnectionPool dbPool;
	protected IDBConnectionPool structDbPool;
	protected IDBConnectionPool forumDbPool;
	protected String dbID;
	protected AppEnv env;
	protected IUsersActivity usersActivity;
	protected IActivity activity;
	protected static String baseTable = "MAINDOCS";

	protected String externalStructureApp;
	private static final String maindocFields = "MAINDOCS.DOCID, DDBID, AUTHOR, PARENTDOCID, PARENTDOCTYPE, REGDATE, DOCTYPE, LASTUPDATE, VIEWTEXT, "
	        + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, VIEWICON, FORM, HAS_ATTACHMENT ";
	private boolean respUsed;

	public Database(AppEnv env, DatabaseType dbType) throws DatabasePoolException, InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		this.env = env;
		if (env.globalSetting.databaseEnable) {
			dbID = env.globalSetting.databaseName;
			connectionURL = env.globalSetting.dbURL;
			dbPool = new DBConnectionPool();
			dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(), env.globalSetting.getDbPassword());

		}

		databaseType = dbType;

	}

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.env = env;
		dbID = env.globalSetting.databaseName;
		connectionURL = env.globalSetting.dbURL;
		dbPool = new DBConnectionPool();
		dbPool.initConnectionPool(env.globalSetting.driver, connectionURL);

		activity = new Activity(this);
		initStructPool();

	}

	public Database(AppEnv env, boolean auth) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.env = env;
		if (env.globalSetting.databaseEnable) {
			dbID = env.globalSetting.databaseName;
			connectionURL = env.globalSetting.dbURL;
			if (auth) {
				dbPool = new DBConnectionPool();
				dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(),
				        env.globalSetting.getDbPassword());
			} else {
				dbPool = new DBConnectionPool();
				dbPool.initConnectionPool(env.globalSetting.driver, connectionURL);
			}

		}

		initStructPool();

	}

	protected void initStructPool() {
		for (ExternalModule module : env.globalSetting.extModuleMap.values()) {
			if (module.getType() == ExternalModuleType.STRUCTURE) {
				externalStructureApp = module.getName();
				Environment.addDelayedInit(this);
			} else {
				Environment.addDelayedInit(this);
			}
		}
		structDbPool = dbPool;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public String getDbID() {
		return dbID;
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

	@Override
	public IStructure getStructure() {
		// TODO Auto-generated method stub
		return null;
	}

}