package kz.lof.dataengine.jpadatabase;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;

public class Structure extends DatabaseCore implements Const {
	public int numberOfLicense = 200;
	public IDBConnectionPool dbPool;

	protected IDatabase db;

	public Structure(IDatabase db, IDBConnectionPool dbPool) {
		this.db = db;
		this.dbPool = dbPool;
	}

}
