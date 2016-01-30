package kz.flabs.dataengine.h2.ftengine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;

public class FTIndexEngineDeployer {
	private IDBConnectionPool dbPool;
	private String schema = "PUBLIC";
	
	
	public FTIndexEngineDeployer(IDBConnectionPool dbPool) {		
		this.dbPool = dbPool;
	}
	
	public void initEngine(){
		Connection conn = dbPool.getConnection();		
		try{
			conn.setAutoCommit(false);
			String sql = "CREATE ALIAS IF NOT EXISTS FTL_INIT FOR \"org.h2.fulltext.FullTextLucene.init\";" +
					" CALL FTL_INIT()";  

			Statement s = conn.createStatement();
			s.execute(sql);
			s.close();
			conn.commit();
		}catch(SQLException e){
			DatabaseUtil.debugErrorPrint(e);
		} finally {	
			dbPool.returnConnection(conn);		
		}

	}

	public void createFTIndex(String tableName, String columnName){
		Connection conn = dbPool.getConnection();
		try{
			conn.setAutoCommit(false);
			String sql = "";			
			if (columnName == null){				
				sql = "CALL FTL_CREATE_INDEX('" + schema + "', '" + tableName + "', NULL);";
			}else{
				sql = "CALL FTL_CREATE_INDEX('" + schema + "', '" + tableName + "', '" + columnName + "');";
			}
			
			Statement s = conn.createStatement();
			s.execute(sql);
			AppEnv.logger.verboseLogEntry("FT undex for " + tableName + " has been created");
			s.close();
			conn.commit();
		}catch(SQLException e){
			//DatabaseUtil.errorPrint(e);
		} finally {		
			dbPool.returnConnection(conn);		
		}
	}
}
