package kz.flabs.dataengine;

import javax.persistence.EntityManagerFactory;

import kz.lof.appenv.AppEnv;

public interface IDatabase {

	IDBConnectionPool getConnectionPool();

	AppEnv getParent();

	EntityManagerFactory getEntityManagerFactory();

	IDatabase getBaseObject();

	IFTIndexEngine getFTSearchEngine();

	String getInfo();

}
