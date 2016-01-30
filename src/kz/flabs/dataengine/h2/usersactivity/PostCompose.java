package kz.flabs.dataengine.h2.usersactivity;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.users.User;

import java.sql.*;


public class PostCompose extends Thread {
    protected IDBConnectionPool dbPool;
    protected Document doc;
    protected String dbID;
    protected User user;
	
	protected PostCompose(IDatabase db, Document doc, User user){
		dbPool = db.getConnectionPool();
		dbID = db.getDbID();
		this.doc = doc;
		this.user = user;
	}


	public void run() {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {			
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, VIEWTEXT, DDBID, CLIENTIP) values ("
				+ UsersActivityType.COMPOSED.getCode() + ",'" + dbID + "', '" + user.getUserID() + "','" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
				"', " + doc.getDocID() + ", " + doc.docType + ", '" + RuntimeObjUtil.cutHTMLText(doc.getViewText().replace("'", "''"), 2048) + "', '" + doc.getDdbID() + "', '" + user.getSession().ipAddr + "')";

			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);	
				for(Field field:doc.fields()){
					String val = field.valueAsText;
					if (val == null) val = "";
					String sqlStatement = "INSERT INTO USERS_ACTIVITY_CHANGES(AID, FIELDNAME, OLDVALUE, NEWVALUE, FIELDTYPE)"
						+ "VALUES (" + key + ", '" + field.name	+ "', '','" + RuntimeObjUtil.cutHTMLText(val.replace("'", "''"), 512) + "', " + field.getTypeAsDatabaseType() + ")";
					PreparedStatement pst = conn.prepareStatement(sqlStatement);
					pst.executeUpdate();
					pst.close();
				}
			}		
			conn.commit();	
			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID,e);		
		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID, e);			
		} finally {			
			dbPool.returnConnection(conn);
		}
	}
}
