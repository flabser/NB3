package kz.flabs.dataengine.mssql.useractivity;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.dataengine.mssql.Database;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.users.User;
import kz.flabs.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.*;

public class PostDelete extends kz.flabs.dataengine.h2.usersactivity.PostDelete {

    protected PostDelete(IDatabase db, BaseDocument deletedDoc, User user) {
        super(db, deletedDoc, user);
    }

    public void run() {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {			
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, VIEWTEXT) values ("
				+ UsersActivityType.DELETED.getCode() + ",'" + dbID + "', '" + user.getUserID() + "','" + Database.sqlDateTimeFormat(new java.util.Date()) +
				"', " + deletedDoc.getDocID() + ", " + deletedDoc.docType + ", '" + Util.removeHTMLTags(deletedDoc.getViewText().replace("'", "''")) + "')";

			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
		        oos.writeObject(deletedDoc);
		        oos.flush();
		        baos.flush();
		        oos.close();
		        baos.close();
		        PreparedStatement pst = conn.prepareStatement("INSERT INTO RECYCLE_BIN (aid, value) VALUES (?, ?)");
		        pst.setInt(1, key);
		        pst.setBytes(2, baos.toByteArray());
		        pst.execute();
		        pst.close();
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
