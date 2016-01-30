package kz.flabs.dataengine.mssql.glossary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IGlossariesTuner;
import kz.flabs.webrule.Lang;


public class GlossariesTuner extends kz.flabs.dataengine.h2.glossary.GlossariesTuner implements IGlossariesTuner {

	public GlossariesTuner(IDatabase db) {	
		super(db);
	}
	
	
	@Override
	public boolean addLang(Lang lang) {
		Statement statement = null;			
		IDBConnectionPool dbPool = db.getConnectionPool();
		Connection conn = dbPool.getConnection();
		try {
			statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			statement.addBatch("ALTER TABLE CUSTOM_FIELDS_GLOSSARY add VALUE_" + lang.id + " nvarchar(512);");	
			statement.executeBatch();
			statement.close(); 
			conn.commit();	
		} catch(Exception e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);		
		}finally {	
			dbPool.returnConnection(conn);
		}



		return false;
	}
}
