package kz.flabs.dataengine.mssql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Executor;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.users.User;
import kz.pchelka.env.Environment;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;


public class TaskOnDatabase extends kz.flabs.dataengine.h2.TaskOnDatabase{	
	private String taskFields = "t.DOCID, t.DOCTYPE, t.VIEWTEXT, t.FORM, t.HAS_ATTACHMENT, t.TASKAUTHOR, t.TASKVN, t.TASKDATE, t.CONTENT, t.COMMENT, t.CONTROLTYPE, t.CTRLDATE, t.ALLCONTROL, t.CYCLECONTROL, t.TASKTYPE, t.ISOLD, t.PARENTDOCID, t.PARENTDOCTYPE, t.LASTUPDATE, t.AUTHOR, t.REGDATE, t.BRIEFCONTENT, t.TASKVN, t.HAR, t.PROJECT, t.DEFAULTRULEID, [t.DBD], " + DatabaseUtil.getViewTextList("t") + ", t.VIEWNUMBER, t.VIEWDATE, t.SIGN, t.SIGNEDFIELDS, [t.CATEGORY], t.APPS, t.CUSTOMER ";
	public TaskOnDatabase(IDatabase db) {
		super(db);
	}

	@Override
	public ArrayList<Task> getTasksByCondition(String condition, Set<String> complexUserID, String absoluteUserID){
		ArrayList<Task> tasks = new ArrayList<Task>();		
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			if (!condition.trim().equals("")) condition = " and " + condition;
			String sql = "SELECT DISTINCT " + taskFields + " FROM TASKS t, READERS_TASKS rt WHERE t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" + condition;
			//String sql = "select * from tasks as t where exists (select * from readers_tasks as rt where t.docid = rt.docid and rt.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) " + condition;
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				Task task = new Task(db, absoluteUserID);
				fillTaskData(rs, task);
				int docID = task.getDocID();
				ResultSet executorsResultSet = conn.createStatement().executeQuery("select * from TASKSEXECUTORS where DOCID=" + docID);
				while (executorsResultSet.next()) {
				//	Executor exec = new Executor(task);
					Executor exec = null;
					exec.setID(executorsResultSet.getString("EXECUTOR"));
					exec.setResetDate(executorsResultSet.getDate("RESETDATE"));
					exec.resetAuthorID = executorsResultSet.getString("RESETAUTHOR");
					exec.comment = executorsResultSet.getString("COMMENT");
					exec.setResponsible(executorsResultSet.getInt("RESPONSIBLE"));
					exec.setPercentOfExecution(executorsResultSet.getInt("EXECPERCENT"));
					task.addExecutor(exec);					
				}
				executorsResultSet.close();				
				db.fillAccessRelatedField(conn, "TASKS", docID, task);
				if (task.hasEditor(complexUserID)){
					task.editMode = EDITMODE_EDIT;
				}
				tasks.add(task);			
			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return tasks;
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
					"CONTROLTYPE, CTRLDATE, ALLCONTROL, CYCLECONTROL, DDBID, VIEWTEXT, DOCTYPE, LASTUPDATE, FORM, TASKTYPE, " +
					" ISOLD, HAR, PROJECT, CATEGORY, BRIEFCONTENT, DEFAULTRULEID, " + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, HAS_ATTACHMENT, APPS, CUSTOMER, SIGN";

			String valuesAsText = doc.parentDocID + 
					", " + doc.parentDocType + 
					",'" + doc.getTaskAuthor() + 
					"', '" + doc.getAuthorID() + 
					"', '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + 
					"','" + doc.getTaskVn().trim() + 
					"','" + Database.sqlDateTimeFormat.format(doc.getTaskDate()) + 
					"', '" + doc.getContent().replace("'", "''") +
					"', '" + doc.getComment().replace("'", "''") + 
			//		"', " + ctrl.getTypeID() + 
					//", '" + Database.sqlDateTimeFormat.format(ctrl.getExecDate()) + 
					", " + (ctrl.getExecDate() != null ? "'" + new Timestamp(ctrl.getExecDate().getTime()) + "'": "null") + 
					", " + ctrl.getAllControl() + ", "	+ ctrl.getCycle() + 
					",'" + doc.getDdbID() + 
					"','" + doc.getViewText().replace("'", "''") + 
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
			/* BLOBS */
			if (id != 0 && !doc.hasField("recID")) {
				PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_TASKS WHERE DOCID = " + id);
				ResultSet rs0 = s0.executeQuery();
				while (rs0.next()) {
					PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_TASKS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE)values(?, ?, ?, ?, ?)");
					s1.setInt(1, key);
					s1.setString(2, rs0.getString("NAME"));
					s1.setString(3, rs0.getString("ORIGINALNAME"));
					s1.setString(4, rs0.getString("CHECKSUM"));
					s1.setBinaryStream(5, rs0.getBinaryStream("VALUE"));
					s1.executeUpdate();
					s1.close();
				}
				rs0.close();
				s0.close();
			} else {
				for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
					PreparedStatement ps = conn
							.prepareStatement("INSERT INTO CUSTOM_BLOBS_TASKS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE)values(?, ?, ?, ?, ?)");
					BlobField bf = blob.getValue();
					for (BlobFile bfile: bf.getFiles()) {
						ps.setInt(1, key);
						ps.setString(2, bf.name);
						ps.setString(3, bfile.originalName);
						ps.setString(4, bfile.checkHash);
						if (!bfile.path.equalsIgnoreCase("")){
							File file = new File(bfile.path);
							FileInputStream fin = new FileInputStream(file);
							ps.setBinaryStream(5, fin, (int)file.length());
							ps.executeUpdate();
							fin.close();
							Environment.fileToDelete.add(bfile.path);
						}else {
							ps.setBytes(5, bfile.getContent());
							ps.executeUpdate();
						}
					}
					ps.close();
				}
			}

			insertAccessRelatedRec("TASKS", key, conn, doc);
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
			
			String update = "update tasks set dbd = DATEDIFF(select ROUND(EXTRACT(DAY from (ctrldate - current_timestamp)))) where docid ="+docID + " and allcontrol = 1";
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
	
	public static void main(String args[]){
		System.out.println("HI");
		String dbURL = "jdbc:h2:C:\\workspace\\NextBase\\Avanti_data\\Avanti;";//
        String driver = "org.h2.Driver";
        
        System.out.println("HI1");
        try {	
        	Class.forName(driver);
            Connection conn = DriverManager.getConnection(dbURL);
        	System.out.println("HI2");
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			conn.setAutoCommit(false);	
			System.out.println("HI3");
			String up = "update tasks set ctrldate = '2012-12-26 00:00:00'";
			String update = "update tasks set dbd = (select ROUND(EXTRACT(EPOCH from ctrldate - current_timestamp) / 86400)) where docid ="+2407;
			String select = "select  dbd,ctrldate from tasks where docid ="+2407;
			s.executeUpdate(up);
			s.executeUpdate(update);
			conn.commit();
			System.out.println(update);
			ResultSet rs = s.executeQuery(select);
			conn.commit();
			System.out.println("HI4");
			while(rs.next()){
				System.out.println(rs.getInt(1));
				System.out.println(rs.getTimestamp(2));
			}
			conn.commit();
			s.close();
		} catch (SQLException e) {
//			AppEnv.logger.errorLogEntry(e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//			dbPool.returnConnection(conn);
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
				  String sign = doc.getSign() != null ? String.format("'%s'", doc.getSign()) : "null";
                String viewTextList = "";
                for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                    viewTextList += "VIEWTEXT" + i + " =  '" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
                }
                if (viewTextList.endsWith(",")) {
                    viewTextList = viewTextList.substring(0, viewTextList.length()-1);
                }
				String update = "update TASKS set PARENTDOCID="
						+ doc.parentDocID + ", PARENTDOCTYPE = "
						+ doc.parentDocType + ", TASKAUTHOR='"
						+ doc.getTaskAuthor() + "', " + "AUTHOR='"
						+ doc.getAuthorID() + "', TASKVN='"
						+ doc.getTaskVn().trim() + "', TASKDATE='"
						+ Database.sqlDateTimeFormat.format(doc.getTaskDate())
						+ "',"
						+ "BRIEFCONTENT='" + doc.getBriefContent().replace("'", "''") + "', "
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
						+ " NOTESID = '" + doc.getDdbID() + "', LASTUPDATE = '"
						+ Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + "', "
						+ " APPS = " + doc.getApps() + ", " 
						+ " CUSTOMER = " + doc.getCustomer()  + ", VIEWTEXT='"
						+ doc.getViewText() + "' "
                        + ", SIGN = " + sign
                        + " where DOCID=" + doc.getDocID();
				s.executeUpdate(update);
				
				this.deleteExecutors(doc.getDocID(), conn);
				
				for(Executor exec: doc.getExecutorsList()){
					this.insertTaskExecutor(doc.getDocID(), exec, conn);
					/*String updateExec = "update TASKSEXECUTORS set EXECUTOR='" + exec.userID + "', " +
							"RESETDATE = " + exec.getResetDateAsDbFormat() + ", " +
							"RESETAUTHOR='" + exec.resetAuthorID + "', " +
							"RESPONSIBLE=" + exec.getResponsible() + ", " +
							"EXECPERCENT=" + exec.getPercentOfExecution() + ", " +
							"COMMENT='"	+ exec.comment + "' where DOCID=" + doc.getDocID() + " and EXECUTOR='" + exec.userID + "'";
					Statement statement = conn.createStatement();
					statement.executeUpdate(updateExec);
					statement.close();*/		
				}
				// =========== BLOBS ===========
				ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
						"FROM CUSTOM_BLOBS_TASKS " +
						"WHERE DOCID = " + doc.getDocID());
				while (blobs.next()) {
					if (!doc.blobFieldsMap.containsKey(blobs.getString("NAME"))) {
						blobs.deleteRow();
						continue;
					}
					BlobField existingBlob = doc.blobFieldsMap.get(blobs.getString("NAME"));
					BlobFile tableFile = new BlobFile();
					tableFile.originalName = blobs.getString("ORIGINALNAME");
					tableFile.checkHash = blobs.getString("CHECKSUM");
					if (existingBlob.findFile(tableFile) == null) {
						blobs.deleteRow();
						continue;
					}
					BlobFile existingFile = existingBlob.findFile(tableFile);
					if (!existingFile.originalName.equals(tableFile.originalName)) {
						blobs.updateString("ORIGINALNAME", existingFile.originalName);
						blobs.updateRow();
					}
				}
				/* now add files that are absent in database */
				for (Entry<String, BlobField> blob: doc.blobFieldsMap.entrySet()) {
					PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
							"FROM CUSTOM_BLOBS_TASKS " +
							"WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					for (BlobFile bfile: blob.getValue().getFiles()) {
						ps.setInt(1, doc.getDocID());
						ps.setString(2, blob.getKey());
						ps.setString(3, bfile.checkHash);
						blobs = ps.executeQuery();
						if (blobs.next()) {
							if (!bfile.originalName.equals(blobs.getString("ORIGINALNAME"))) {
								blobs.updateString("ORIGINALNAME", bfile.originalName);
								blobs.updateRow();
							}
						} else {
							blobs.moveToInsertRow();
							blobs.updateInt("DOCID", doc.getDocID());
							blobs.updateString("NAME", blob.getKey());
							blobs.updateString("ORIGINALNAME", bfile.originalName);
							blobs.updateString("CHECKSUM", bfile.checkHash);
							File file = new File(bfile.path);
							InputStream is = new FileInputStream(file);
							blobs.updateBinaryStream("VALUE", is, (int)file.length());
							blobs.insertRow();
							is.close();
						}
						Environment.fileToDelete.add(bfile.path);
					}
					ps.close();
				}
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
