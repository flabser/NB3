package kz.flabs.dataengine;

import javax.persistence.EntityManagerFactory;

import kz.lof.appenv.AppEnv;

public interface IDatabase {

	int getVersion();

	IDBConnectionPool getConnectionPool();

	AppEnv getParent();

	EntityManagerFactory getEntityManagerFactory();

	IDatabase getBaseObject();

	IFTIndexEngine getFTSearchEngine();

	IStructure getStructure();

}
