package kz.flabs.dataengine.h2.glossary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IGlossariesTuner;
import kz.flabs.webrule.Lang;


public class GlossariesTuner implements IGlossariesTuner {
	protected IDatabase db;

	public GlossariesTuner(IDatabase db) {	
		this.db = db;
	}

	@Override
	public ArrayList<String> getSupportedLangs() {
		ArrayList<String> supportedLangs = new ArrayList<String>();
		IDBConnectionPool dbPool = db.getConnectionPool();
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);	
			ResultSet rs = s.executeQuery("select * from CUSTOM_FIELDS_GLOSSARY");
			ResultSetMetaData md = rs.getMetaData();
			int cnt = md.getColumnCount();
			for(int i=1; i <= cnt; i++){
				String val = md.getColumnName(i);
				if (val.contains("VALUE_")){
					int b = val.indexOf("_") + 1;
					int e = val.length();
					supportedLangs.add(val.substring(b, e));
				}
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return supportedLangs;
	}

	@Override
	public boolean addLang(Lang lang) {
		Statement statement = null;			
		IDBConnectionPool dbPool = db.getConnectionPool();
		Connection conn = dbPool.getConnection();
		try {
			statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			statement.addBatch("ALTER TABLE CUSTOM_FIELDS_GLOSSARY add VALUE_" + lang.id + " varchar_ignorecase(512);");	
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

	public static void main(String[] args){
		String val = "VALUE_CH";
		int b = val.indexOf("_") + 1;
		int e = val.length();
		System.out.println(val.substring(b, e));

	}

}
