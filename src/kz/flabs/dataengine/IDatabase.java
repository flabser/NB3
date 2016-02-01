package kz.flabs.dataengine;

import javax.persistence.EntityManagerFactory;

import kz.flabs.appenv.AppEnv;

public interface IDatabase {

	int getVersion();

	String getDbID();

	IDBConnectionPool getConnectionPool();

	AppEnv getParent();

	EntityManagerFactory getEntityManagerFactory();

	IDatabase getBaseObject();

	IFTIndexEngine getFTSearchEngine();

	IStructure getStructure();

}
