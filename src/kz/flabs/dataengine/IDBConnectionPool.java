package kz.flabs.dataengine;

import java.sql.Connection;

public interface IDBConnectionPool {
	void initConnectionPool(String driver, String dbURL, String userName, String password) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException; 
	void initConnectionPool(String driver, String dbURL) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException;
	Connection getConnection();	
	void returnConnection(Connection con);
	int getNumActive();
	public String toXML();

	String getDatabaseVersion();

	void closeAll();
	void close(Connection conn);
	DatabaseType getDatabaseType();
}
