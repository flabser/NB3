package kz.flabs.dataengine.h2.help;

import kz.flabs.dataengine.*;

public class Help implements IHelp {
	private IDBConnectionPool dbPool;
	private IDatabase db;
	
	public Help(IDatabase db) {
		this.db = db;
		this.dbPool = db.getConnectionPool();
	}
}
