package kz.flabs.dataengine.mssql.useractivity;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.dataengine.mssql.Database;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.users.User;
import kz.flabs.util.Util;

import java.sql.*;

public class PostModify extends kz.flabs.dataengine.h2.usersactivity.PostModify {

    protected PostModify(IDatabase db, Document oldDoc, Document modifiedDoc, User user) {
        super(db, oldDoc, modifiedDoc, user);
    }


    public void run() {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {			
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, VIEWTEXT, DDBID) values ("
				+ UsersActivityType.MODIFIED.getCode() + ",'" + dbID + "', '" + user.getUserID() + "','" + Database.sqlDateTimeFormat(new java.util.Date()) +
				"', " + modifiedDoc.getDocID() + ", " + modifiedDoc.docType + ", '" + Util.removeHTMLTags(modifiedDoc.getViewText().replace("'", "''")) + "', '" + modifiedDoc.getDdbID() + "')";

			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);				
				for(Field field: modifiedDoc.fields()){					
					Field oldField = oldDoc.getFieldsMap().get(field.name);
					if (oldField != null && oldField.valueAsText != null && (!oldField.valueAsText.equals(field.valueAsText))){						
						String sqlStatement = "INSERT INTO USERS_ACTIVITY_CHANGES(AID, FIELDNAME, OLDVALUE, NEWVALUE, FIELDTYPE)"
							+ "VALUES (" + key + ", '" + field.name	+ "', '" + Util.removeHTMLTags(oldField.valueAsText) + "','" + Util.removeHTMLTags(field.valueAsText) + "', " + field.getTypeAsDatabaseType() + ")";
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
			DatabaseUtil.errorPrint(dbID,e);			
		} finally {			
			dbPool.returnConnection(conn);
		}
		//return key;
	}
}
