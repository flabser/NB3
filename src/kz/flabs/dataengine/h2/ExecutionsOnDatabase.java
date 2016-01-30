package kz.flabs.dataengine.h2;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.users.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Set;

public class ExecutionsOnDatabase  extends DatabaseCore  implements IExecutions, Const {
	protected IDBConnectionPool dbPool;
	protected IDatabase db;	
	protected static String baseTable = "EXECUTIONS"; 

	public ExecutionsOnDatabase(IDatabase db) {
		this.db = db;
		this.dbPool = db.getConnectionPool();
	}

	@Override
	public Execution getExecutionByID(int docID, Set<String> complexUserID, String absoluteUserID)throws DocumentAccessException {
		Connection conn = dbPool.getConnection();
		Execution exec = new Execution(db, absoluteUserID);
		try {
			conn.setAutoCommit(false);
			/*String sql = "select distinct " + executionFields + " from EXECUTIONS e, READERS_EXECUTIONS re where e.DOCID = " + docID +
			" AND re.DOCID = e.DOCID AND re.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/
			String sql = "select * from EXECUTIONS e where exists (select * from readers_executions as re where re.DOCID = " + docID +
					" AND re.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) AND e.DOCID = " + docID;
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				exec = fillExecutionData(rs, exec);
			}
			
			db.fillAccessRelatedField(conn, "EXECUTIONS", docID, exec);
			if (exec.hasEditor(complexUserID)) {
				exec.editMode = EDITMODE_EDIT;
			}
			
			db.fillBlobs(conn, exec, baseTable);
			
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}finally{		
			dbPool.returnConnection(conn);
		}
		return exec;
	}

	@Override
	public int insertExecution(Execution doc, User user) throws DocumentException {
		Connection conn = dbPool.getConnection();
		int id = doc.getDocID();
		int key = 0;
		try {
			conn.setAutoCommit(false);
			
			String fieldsAsText = "AUTHOR, REGDATE, EXECUTOR, REPORT, FINISHDATE, PARENTDOCID, DDBID, VIEWTEXT," +
					" DOCTYPE, PARENTDOCTYPE, NOMENTYPE, DEFAULTRULEID, LASTUPDATE, FORM, HAS_ATTACHMENT";
			String valuesAsText = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '"
					+ Database.sqlDateTimeFormat.format(doc.getLastUpdate())
					+ "', '"
					+ doc.form + "', ?";
			
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
			pst.setString(7, doc.getDdbID());
			pst.setString(8, doc.getViewText());
			pst.setInt(9, Const.DOCTYPE_EXECUTION);
			pst.setInt(10, doc.parentDocType);
			pst.setInt(11, doc.getNomenType());
			pst.setString(12, doc.getDefaultRuleID());
			pst.setInt(13, doc.blobFieldsMap.size());
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			} else {
				key = id;
			}

			db.insertBlobTables(conn, id, key, doc, baseTable);
			db.insertToAccessTables(conn, baseTable, key, doc);
			//CachePool.flush();
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
						+ " PARENTDOCID = " + doc.parentDocID + ", NOTESID = '"
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

	Execution fillExecutionData(ResultSet rs, Execution exec) {
		try {
			exec.setExecutor(rs.getString("EXECUTOR"));
			exec.setReport(rs.getString("REPORT"));
			exec.setFinishDate(rs.getTimestamp("FINISHDATE"));
			exec.setNomenType(rs.getInt("NOMENTYPE"));
			exec.docType = Const.DOCTYPE_EXECUTION;
			exec.setDefaultRuleID(rs.getString("DEFAULTRULEID"));
			fillViewTextData(rs, exec);
			fillSysData(rs, exec);
			exec.addStringField("form",rs.getString("FORM"));
			exec.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return exec;
	}
	
	
	public void resetTopic(int executionID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "update executions set topicid = null where docid = ?";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, executionID);
			pst.executeQuery();
			pst.close();
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
	
	public void setTopic(int topicID, int executionID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "update executions set topicid = ? where docid = ?";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, topicID);
			pst.setInt(2, executionID);
			pst.executeQuery();
			pst.close();
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

}
