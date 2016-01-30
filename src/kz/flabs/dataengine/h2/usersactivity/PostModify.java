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
import kz.flabs.util.Util;

import java.sql.*;

public class PostModify extends Thread {
	protected IDBConnectionPool dbPool;
    protected String dbID;
    protected Document oldDoc;
    protected Document modifiedDoc;
    protected User user;
	
	protected PostModify(IDatabase db, Document oldDoc, Document modifiedDoc, User user){
		dbPool = db.getConnectionPool();
		dbID = db.getDbID();
		this.oldDoc = oldDoc;
		this.modifiedDoc = modifiedDoc;
		this.user = user;
	}


	public void run() {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {			
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, VIEWTEXT, DDBID, CLIENTIP) values ("
				+ UsersActivityType.MODIFIED.getCode() + ",'" + dbID + "', '" + user.getUserID() + "','" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
				"', " + modifiedDoc.getDocID() + ", " + modifiedDoc.docType + ", '" + RuntimeObjUtil.cutHTMLText(modifiedDoc.getViewText().replace("'", "''"),512) + "', '" + modifiedDoc.getDdbID() + "', '"+ user.getSession().ipAddr +"')";

			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);				
				for(Field field: modifiedDoc.fields()){					
					Field oldField = oldDoc.getFieldsMap().get(field.name);
					if (oldField != null && oldField.valueAsText != null && (!oldField.valueAsText.equals(field.valueAsText))){						
						String sqlStatement = "INSERT INTO USERS_ACTIVITY_CHANGES(AID, FIELDNAME, OLDVALUE, NEWVALUE, FIELDTYPE)"
							+ "VALUES (" + key + ", '" + field.name	+ "', '" + Util.removeHTMLTags(oldField.valueAsText) + "','" + RuntimeObjUtil.cutHTMLText(field.valueAsText,512) + "', " + field.getTypeAsDatabaseType() + ")";
						PreparedStatement pst = conn.prepareStatement(sqlStatement);
						pst.executeUpdate();
						pst.close();
					}					
				}
			}		
			conn.commit();		
			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID,e);		
		} catch (Exception e) {
            e.printStackTrace();
			DatabaseUtil.errorPrint(dbID,e);			
		} finally {			
			dbPool.returnConnection(conn);
		}
		//return key;
	}
}
