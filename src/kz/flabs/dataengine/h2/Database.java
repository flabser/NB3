package kz.flabs.dataengine.h2;

import javax.persistence.EntityManagerFactory;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;

public class Database extends DatabaseCore implements IDatabase, Const {

	// TODO need to improve to make it more secure
	protected static String dbUser = "ss";
	protected static String dbPwd = "maza";
	protected static String connectionURL = "";
	protected IDBConnectionPool dbPool;
	protected String dbID;
	protected AppEnv env;
	protected static String baseTable = "MAINDOCS";

	protected String externalStructureApp;

	public Database() {

	}

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.env = env;
		dbPool = new DBConnectionPool();
		dbPool.initConnectionPool(EnvConst.JDBC_DRIVER, connectionURL, dbUser, dbPwd);

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
		return this;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		return null;
	}

	@Override
	public String getInfo() {
		return connectionURL;
	}

}