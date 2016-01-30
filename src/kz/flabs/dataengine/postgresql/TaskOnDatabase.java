package kz.flabs.dataengine.postgresql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Executor;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.users.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

public class TaskOnDatabase extends kz.flabs.dataengine.h2.TaskOnDatabase{	

	public TaskOnDatabase(IDatabase db) {
		super(db);
	}

	@Override
	public int insertTask(Task doc, User user) {
		int key = 0;
		int id = doc.getDocID();
		Date viewDate = doc.getViewDate();
		Control ctrl = doc.getControl();

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
            String sign = doc.getSign() != null ? String.format("'%s'", doc.getSign()) : "null";
			String fieldsAsText = "PARENTDOCID, PARENTDOCTYPE, TASKAUTHOR, AUTHOR, REGDATE, TASKVN, TASKDATE, CONTENT, COMMENT, " +
					"CONTROLTYPE, CTRLDATE, ALLCONTROL, CYCLECONTROL, VIEWTEXT, DOCTYPE, LASTUPDATE, FORM, TASKTYPE, " +
					" ISOLD, HAR, PROJECT, CATEGORY, BRIEFCONTENT, DEFAULTRULEID, " + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, HAS_ATTACHMENT, APPS, CUSTOMER, SIGN ";

			String valuesAsText = doc.parentDocID + 
					", " + doc.parentDocType + 
					",'" + doc.getTaskAuthor() + 
					"', '" + doc.getAuthorID() + 
					"', '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + 
					"','" + doc.getTaskVn().trim() + 
					"','" + Database.sqlDateTimeFormat.format(doc.getTaskDate()) + 
					"', '" + doc.getContent().replace("'", "''") +
					"', '', " + ("".equalsIgnoreCase(ctrl.getTypeID()) ? 0 : ctrl.getTypeID()) +
					//", '" + Database.sqlDateTimeFormat.format(ctrl.getExecDate()) + 
					", " + (ctrl.getExecDate() != null ? "'" + new Timestamp(ctrl.getExecDate().getTime()) + "'": "null") +
					", " + ctrl.getAllControl() + ", "	+ ctrl.getCycle() + 
					",'" + doc.getViewText().replace("'", "''") + 
					"', " + Const.DOCTYPE_TASK + 
					", '" + Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + 
					"','" + doc.form +
					"', " + doc.getResolTypeAsInt() + 
					", " + ctrl.getOld() + 
					", " + (doc.getHar() != 0 ? doc.getHar() : "null") + 
					"," + (doc.getProject() != 0 ? doc.getProject() : "null") + 
					"," + (doc.getCategory() != 0 ? doc.getCategory() : "null") + 
					",'" + doc.getBriefContent().replace("'", "''") + 
					"', '" + doc.getDefaultRuleID() + 
					"', " + DatabaseUtil.getViewTextValues(doc) + ", " + doc.getViewNumber() +
					", " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'": "null") + 
					", " + doc.blobFieldsMap.size() + ", " + doc.getApps() + ", " + doc.getCustomer() + ", " + sign;

			if (id != 0 && doc.hasField("recID")){
				fieldsAsText = "DOCID, " + fieldsAsText;
				valuesAsText = id + ", " + valuesAsText;
			}

			String sql = "insert into TASKS(" + fieldsAsText + ") values (" + valuesAsText + ")";

			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}

			for(Executor exec: doc.getExecutorsList()){
				String execSQL = "insert into TASKSEXECUTORS(DOCID, EXECUTOR, RESETDATE, RESETAUTHOR, COMMENT, RESPONSIBLE, EXECPERCENT) values ("
						+ key + ", '" + exec.getID() + "'," + exec.getResetDateAsDbFormat() + ", '" + exec.resetAuthorID + "','" + exec.comment.replace("'", "''") + "', " + exec.getResponsible() + ", " + exec.getPercentOfExecution() + ")";

				Statement statement = conn.createStatement();
				statement.executeUpdate(execSQL);
				statement.close();			
			}

            this.db.insertBlobTables(conn, id, key, doc, baseTable);
			insertAccessRelatedRec(baseTable, key, conn, doc);
			conn.commit();
			s.close();
			//CachePool.flush();
			if (!doc.hasField("recID")){
				IUsersActivity ua = db.getUserActivity();
				ua.postCompose(doc, user);
				ua.postMarkRead(key, doc.docType, user);
			}
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (Exception e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {			
			dbPool.returnConnection(conn);
		}
		return key;

	}

	@Override
	public int recalculate() {	
			Connection conn = dbPool.getConnection();
			try {				
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);	
				String update = "update tasks set dbd = (select ROUND(EXTRACT(EPOCH from ctrldate - current_timestamp) / 86400)) where allcontrol = 1";
				s.executeUpdate(update);
				conn.commit();
				s.close();
			} catch (SQLException e) {
				AppEnv.logger.errorLogEntry(e);
				return -1;
			} finally {
				dbPool.returnConnection(conn);
			}
			return 0;		
	}

	public int recalculate(int docID) {	
		Connection conn = dbPool.getConnection();
		try {				
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			conn.setAutoCommit(false);	
			
			String update = "update tasks set dbd = (select ROUND(EXTRACT(DAY from (ctrldate - current_timestamp)))) where docid ="+docID + " and allcontrol = 1";
			String select = "select dbd from tasks where docid ="+docID;
	
			s.executeUpdate(update);
			conn.commit();
			ResultSet rs = s.executeQuery(select);
			conn.commit();
			if(rs.next()){
				return rs.getInt(1);
			} else {
				return 0;
			}
		} catch (SQLException e) {
			AppEnv.logger.errorLogEntry(e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}	
}
	
	protected int deleteExecutors(int taskKey, Connection conn) {
		String sql = "delete from TASKSEXECUTORS where docid = ?";
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, taskKey);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return -1;
	}
	
	protected int insertTaskExecutor(int taskKey, Executor exec, Connection conn) {
		String insertExec = "insert into TASKSEXECUTORS (EXECUTOR, RESETDATE, RESETAUTHOR, RESPONSIBLE, EXECPERCENT, COMMENT, DOCID) values (?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pst = conn.prepareStatement(insertExec);
			pst.setString(1, exec.getID());

			if (exec.getResetDate() == null) {
				pst.setNull(2, Types.TIMESTAMP);
			} else {
				pst.setTimestamp(2, new Timestamp(exec.getResetDate().getTime()));
			}

			pst.setString(3, exec.resetAuthorID);
			pst.setInt(4, exec.getResponsible());
			pst.setInt(5, exec.getPercentOfExecution());
			pst.setString(6, exec.comment);
			pst.setInt(7, taskKey);
			pst.executeUpdate();
			pst.close();
		} catch(SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return -1;
	}

	@Override
	public int updateTask(Task doc, User user) throws DocumentAccessException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Task oldDoc = this.getTaskByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
			Control ctrl = doc.getControl();
			Connection conn = dbPool.getConnection();
			try {
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);
				Date viewDate = doc.getViewDate();
                String viewTextList = "";
                for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                    viewTextList += "VIEWTEXT" + i + " =  '" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
                }
                if (viewTextList.endsWith(",")) {
                    viewTextList = viewTextList.substring(0, viewTextList.length()-1);
                }
                String sign = doc.getSign() != null && doc.getSign().length() > 0 ? String.format("'%s'", doc.getSign()) : "null";
				String update = "update TASKS set PARENTDOCID="
						+ doc.parentDocID + ", PARENTDOCTYPE = "
						+ doc.parentDocType + ", TASKAUTHOR='"
						+ doc.getTaskAuthor() + "', " + "AUTHOR='"
						+ doc.getAuthorID() + "', TASKVN='"
						+ doc.getTaskVn().trim() + "', TASKDATE='"
						+ Database.sqlDateTimeFormat.format(doc.getTaskDate())
						+ "',"
						+ "BRIEFCONTENT='" + doc.getBriefContent().replace("'","''") + "', "
						+ "CONTENT='" + doc.getContent().replace("'", "''") + "', COMMENT='"
						+ doc.getComment().replace("'", "''") + "', CONTROLTYPE="
						+ ctrl.getTypeID() + ", " + "CTRLDATE="
						+ (ctrl.getExecDate() != null ? "'" + new Timestamp(ctrl.getExecDate().getTime()) + "'": "null") 
						+ ", HAR = " + (doc.getHar() != 0 ? doc.getHar() : "null")
						+ ", DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'"
						+ ", " + viewTextList
						+ ", VIEWNUMBER = " + doc.getViewNumber()
						+ ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'": "null") + ""
						+ ", PROJECT = " + (doc.getProject() != 0 ? doc.getProject() : "null")
						+ ", CATEGORY = " + (doc.getCategory() != 0 ? doc.getCategory() : "null")
						+ ", CYCLECONTROL=" + ctrl.getCycle() + ", TASKTYPE=" + doc.getResolTypeAsInt()
						+ ", ALLCONTROL=" + ctrl.getAllControl() + ", ISOLD=" + ctrl.getOld() + ","
						+ " DDBID = '" + doc.getDdbID() + "', LASTUPDATE = '"
						+ Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + "', "
						+ " APPS = " + doc.getApps() + ", " 
						+ " CUSTOMER = " + doc.getCustomer()  
						+ ", VIEWTEXT='" + doc.getViewText().replace("'", "''") + "' "
                        + ", SIGN = " + sign
                        + " where DOCID=" + doc.getDocID();
				s.executeUpdate(update);
				
				this.deleteExecutors(doc.getDocID(), conn);
				
				for(Executor exec: doc.getExecutorsList()){
					this.insertTaskExecutor(doc.getDocID(), exec, conn);
				}

				db.updateBlobTables(conn, doc, baseTable);			
				db.updateAccessTables(conn, doc, baseTable);
				conn.commit();
				s.close();
				IUsersActivity ua = db.getUserActivity();
				ua.postModify(oldDoc, doc, user);
			} catch (DocumentException de) {
				AppEnv.logger.errorLogEntry(de);
				return -1;
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(db.getDbID(), e);
				return -1;
			} catch (FileNotFoundException fe) {
				AppEnv.logger.errorLogEntry(fe);
				return -1;
			} catch (IOException ioe) {
				AppEnv.logger.errorLogEntry(ioe);
				return -1;
            } finally {
				dbPool.returnConnection(conn);
			}
			return doc.getDocID();
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}
	}
}
