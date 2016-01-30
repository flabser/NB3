package kz.flabs.dataengine.postgresql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.users.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ExecutionsOnDatabase extends kz.flabs.dataengine.h2.ExecutionsOnDatabase implements IExecutions, Const {



	public ExecutionsOnDatabase(IDatabase db) {
		super(db);
	}

	@Override
	public int insertExecution(Execution doc, User user) throws DocumentException {
		Connection conn = dbPool.getConnection();
		int id = doc.getDocID();
		int key = 0;
		try {
			conn.setAutoCommit(false);

			String fieldsAsText = "AUTHOR, REGDATE, EXECUTOR, REPORT, FINISHDATE, PARENTDOCID, VIEWTEXT,"
					+ " DOCTYPE, PARENTDOCTYPE, NOMENTYPE, LASTUPDATE, FORM, DEFAULTRULEID, HAS_ATTACHMENT";
			String valuesAsText = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '"
					+ Database.sqlDateTimeFormat.format(doc.getLastUpdate())
					+ "', '"
					+ doc.form + "', " + "?, ?";

			if (id != 0 && doc.hasField("recID")){
				fieldsAsText = "DOCID, " + fieldsAsText;
				valuesAsText = id + ", " + valuesAsText;
			}

			String sql = "insert into EXECUTIONS(" + fieldsAsText + ")" + "values(" + valuesAsText + ")";

			PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, doc.getAuthorID());
			pst.setTimestamp(2, new java.sql.Timestamp(doc.getRegDate().getTime()));
			pst.setString(3, doc.executor);
			pst.setString(4, doc.report);
			pst.setTimestamp(5, new java.sql.Timestamp(doc.getFinishDate().getTime()));			
			pst.setInt(6, doc.parentDocID);
			pst.setString(7, doc.getViewText());
			pst.setInt(8, Const.DOCTYPE_EXECUTION);
			pst.setInt(9, doc.parentDocType);
			pst.setInt(10, doc.getNomenType());
			pst.setString(11, doc.getDefaultRuleID());
			pst.setInt(12, doc.blobFieldsMap.size());
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			db.insertBlobTables(conn, id, key, doc, baseTable);
			db.insertToAccessTables(conn, baseTable, key, doc);
			conn.commit();
			rs.close();
			pst.close();
			if (!doc.hasField("recID")){
				IUsersActivity ua = db.getUserActivity();
				ua.postCompose(doc, user);
				ua.postMarkRead(key, doc.docType, user);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (FileNotFoundException fe) {
			AppEnv.logger.errorLogEntry(fe);
			return -1;
		} catch (IOException ioe) {
			AppEnv.logger.errorLogEntry(ioe);
			return -1;
		}finally{	
			dbPool.returnConnection(conn);
		}
		return key;
	}

	@Override
	public int updateExecution(Execution doc, User user) throws DocumentAccessException, DocumentException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Connection conn = dbPool.getConnection();
			try {			
				conn.setAutoCommit(false);
				String sql = "update EXECUTIONS set LASTUPDATE ='"
						+ Database.sqlDateTimeFormat.format(doc.getLastUpdate())
						+ "', AUTHOR = '" + doc.getAuthorID() + "', EXECUTOR = '"
						+ doc.executor + "', REPORT = '" + doc.report + "',"
						+ " NOMENTYPE = " + doc.getNomenType() + ", "  
						+ " PARENTDOCID = " + doc.parentDocID + ", DDBID = '"
						+ doc.getDdbID() + "', PARENTDOCTYPE = "
						+ doc.parentDocType + ", VIEWTEXT='" + doc.getViewText()
						+ "', DEFAULTRULEID = '" + doc.getDefaultRuleID()
						+ "' where DOCID = " + doc.getDocID();
				PreparedStatement pst = conn.prepareStatement(sql);
				pst.executeUpdate();
				pst.close();
				db.updateBlobTables(conn, doc, baseTable);
				db.updateAccessTables(conn, doc, baseTable);
				conn.commit();
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(db.getDbID(), e);
				return -1;
			} catch (FileNotFoundException fe) {
				AppEnv.logger.errorLogEntry(fe);
				return -1;
			} catch (IOException ioe) {
				AppEnv.logger.errorLogEntry(ioe);
				return -1;
			}finally{		
				dbPool.returnConnection(conn);
			}
			return doc.getDocID();
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}
	}


}
