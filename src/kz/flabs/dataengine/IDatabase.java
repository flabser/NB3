package kz.flabs.dataengine;

import javax.persistence.EntityManagerFactory;

public interface IDatabase {

	IDBConnectionPool getConnectionPool();

	EntityManagerFactory getEntityManagerFactory();

	IDatabase getBaseObject();

	IFTIndexEngine getFTSearchEngine();

	String getInfo();

}
