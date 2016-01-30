package kz.flabs.dataengine.h2.usersactivity;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.users.User;
import kz.flabs.util.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PostUndelete extends Thread {
    protected IDBConnectionPool dbPool;
    protected BaseDocument recoverDoc;
    protected User user;
    protected String dbID;

	protected PostUndelete(IDatabase db, BaseDocument recoverDoc, User user){
		dbPool = db.getConnectionPool();
		dbID = db.getDbID();
		this.recoverDoc = recoverDoc;
		this.user = user;
	}


	public void run() {
		Connection conn = dbPool.getConnection();
		try {			
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, VIEWTEXT, DDBID, CLIENTIP) values ("
				+ UsersActivityType.UNDELETED.getCode() + ",'" + dbID + "', '" + user.getUserID() + "','" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
				"', " + recoverDoc.getDocID() + ", " + recoverDoc.docType + ", '" + Util.removeHTMLTags(recoverDoc.getViewText().replace("'", "''")) + "', '" + recoverDoc.getDdbID() + "', '"+ user.getSession().ipAddr +"')";

			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);	
			sql = "DELETE FROM RECYCLE_BIN WHERE AID = " + recoverDoc.getValueAsInteger("recID");
			s.execute(sql);
			conn.commit();	
			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID,e);		
		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID,e);			
		} finally {			
			dbPool.returnConnection(conn);
		}
		//return key;
	}
}
