package kz.flabs.dataengine.h2;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Executor;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.queries.CacheInitiatorType;
import kz.flabs.runtimeobj.queries.CachePool;
import kz.flabs.runtimeobj.queries.QueryCache;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.Reader;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.query.QueryFieldRule;
import org.h2.jdbc.JdbcSQLException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class TaskOnDatabase extends DatabaseCore implements ITasks, Const{
	protected IDBConnectionPool dbPool;
	protected IDatabase db;

	protected static String baseTable = "TASKS"; 
	private IUsersActivity usersActivity;
	private String taskFields = "t.DDBID, t.DOCID, t.DOCTYPE, t.DDBID, t.VIEWTEXT, t.FORM, t.HAS_ATTACHMENT, t.TASKAUTHOR, t.TASKVN, t.TASKDATE, t.CONTENT, t.COMMENT, t.CONTROLTYPE, t.CTRLDATE, t.ALLCONTROL, t.CYCLECONTROL, t.TASKTYPE, t.ISOLD, t.PARENTDOCID, t.PARENTDOCTYPE, t.LASTUPDATE, t.AUTHOR, t.REGDATE, t.BRIEFCONTENT, t.TASKVN, t.HAR, t.PROJECT, t.DEFAULTRULEID, t.DBD, " + DatabaseUtil.getViewTextList("t") + ", t.VIEWNUMBER, t.VIEWDATE, t.SIGN, t.SIGNEDFIELDS, t.CATEGORY, t.APPS, t.CUSTOMER ";

	public TaskOnDatabase(IDatabase db) {
		this.db = db;
		usersActivity = db.getUserActivity();
		this.dbPool = db.getConnectionPool();
	}

	public Task getTaskByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException {
		Task task = new Task(db, absoluteUserID);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();		
			/*String sql = "select DISTINCT " + taskFields + " from TASKS t, READERS_TASKS rt where t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ") and t.DOCID = " + docID;*/
			String sql = "select * from TASKS as t where exists (select * from readers_tasks rt where rt.docid = " + docID + " and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" + " and t.docid = " + docID;
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				fillTaskData(rs, task);
				ResultSet executorsResultSet = conn.createStatement().executeQuery("select * from TASKSEXECUTORS where DOCID=" + docID);
				while (executorsResultSet.next()) {
					//Executor exec = new Executor(task);
					Executor exec = null;
					exec.setID(executorsResultSet.getString("EXECUTOR"));
					exec.setResetDate(executorsResultSet.getTimestamp("RESETDATE"));
					exec.resetAuthorID = executorsResultSet.getString("RESETAUTHOR");
					exec.comment = executorsResultSet.getString("COMMENT");
					exec.setResponsible(executorsResultSet.getInt("RESPONSIBLE"));
					exec.setPercentOfExecution(executorsResultSet.getInt("EXECPERCENT"));
					task.addExecutor(exec);					
				}
				executorsResultSet.close();	
				rs.close();

				db.fillAccessRelatedField(conn, "TASKS", docID, task);
				if (task.hasEditor(complexUserID)) {
					task.editMode = EDITMODE_EDIT;
				}

				db.fillBlobs(conn, task, "TASKS");

			}else{			
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
			}
			conn.commit();
			rs.close();
			statement.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return task;
	}

	public void resetTopic(int maindocID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "update tasks set topicid = null where docid = ?";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, maindocID);
			pst.executeQuery();
			pst.close();
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	public void setTopic(int topicID, int tasksID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "update tasks set topicid = ? where docid = ?";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, topicID);
			pst.setInt(2, tasksID);
			pst.executeQuery();
			pst.close();
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}


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
					//Executor exec = new Executor(task);
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

	public int getTasksCountByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID){
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = condition.getSQLCount(complexUserID);			
			//	QueryCache qc =  CachePool.getQueryCache(sql, absoluteUserID);
			//	if (qc == null){
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();								
			CachePool.update(count, sql, absoluteUserID, CacheInitiatorType.QUERY);
			//	}else{
			//		count = qc.getIntContent();
			//	}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return count;
	}

	public int getTasksCountByCondition(String condition, Set<String> complexUserID, String absoluteUserID){
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			if (!condition.trim().equals("")) condition = " and " + condition;
			String sql = "SELECT count(DISTINCT t.docid) FROM TASKS t, READERS_TASKS rt WHERE t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" + condition;
			//String sql = "SELECT count(*) FROM TASKS as t WHERE EXISTS (select * from readers_tasks as rt where rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ") and rt.DOCID = t.DOCID " + condition + ")";
			QueryCache qc =  CachePool.getQueryCache(sql, absoluteUserID);
			if (qc == null){
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					count = rs.getInt(1);
				}
				rs.close();								
				CachePool.update(count, sql, absoluteUserID, CacheInitiatorType.QUERY);
			}else{
				count = qc.getIntContent();
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return count;
	}

	/*	public StringBuffer getExecutorsAsXML(){
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try{

		}
	}*/

	public StringBuffer getTasksByCondition(String condition, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, Set<DocID> toExpandResponses, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			if (!condition.trim().equals("")) condition = " and " + condition;
			String sql = "SELECT DISTINCT " + taskFields + " FROM TASKS t, READERS_TASKS rt WHERE t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" + 
					condition + " ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;
			/*String sql = "SELECT * FROM TASKS t WHERE EXISTS (SELECT DOCID FROM READERS_TASKS rt where t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" +
			condition + " ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;*/

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				String form = rs.getString("FORM");
                String id = rs.getString("DDBID");
				StringBuffer value = new StringBuffer(1000);
				
				for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value)});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}

				if (db.hasResponse(conn, docID, DOCTYPE_TASK, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
					if (toExpandResponses.size() > 0) {
						for (DocID doc : toExpandResponses) {
							if (doc.id == docID && doc.type == DOCTYPE_TASK) {
								DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
								value.append("<responses>" + responses.xmlContent + "</responses>");
							}
						}
					}
				}

				xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + DOCTYPE_TASK + "\"  " +
						"docid=\"" + docID + "\" " +
						"id=\"" + id + "\" " +
						"url=\"Provider?type=edit&amp;element=task&amp;id=" + form + "&amp;key="	+ docID + "\" " +
						"><dbd>" + rs.getInt("DBD") + "</dbd>" + getViewContent(rs) + value);
				xmlContent.append("</entry>");	
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	public StringBuffer getTasksByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, Set<DocID> toExpandResponses, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			/*if (!condition.trim().equals("")) condition = " and " + condition;
			String sql = "SELECT DISTINCT " + taskFields + " FROM TASKS t, READERS_TASKS rt WHERE t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" + 
			condition + " ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;*/
			/*String sql = "SELECT * FROM TASKS t WHERE EXISTS (SELECT DOCID FROM READERS_TASKS rt where t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" +
			condition + " ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;*/
			String sql = condition.getSQL(complexUserID) + " LIMIT " + pageSize + " OFFSET " + offset;		
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				String form = rs.getString("FORM");
                String id = rs.getString("DDBID");
				StringBuffer value = new StringBuffer(1000);
			
				/*for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value)});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}
*/
				if (db.hasResponse(conn, docID, DOCTYPE_TASK, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
					if (toExpandResponses.size() > 0) {
						for (DocID doc : toExpandResponses) {
							if (doc.id == docID && doc.type == DOCTYPE_TASK) {
								DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
								value.append("<responses>" + responses.xmlContent + "</responses>");
							}
						}
					}
				}

			     Employer emp =  db.getStructure().getAppUser(absoluteUserID);
			     
				xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + DOCTYPE_TASK + "\"  " +
						"docid=\"" + docID + "\" " +
						"id=\"" + id + "\" " +
                        "allcontrol=\"" + rs.getInt("ALLCONTROL") + "\" " +
						"favourites=\"" + db.isFavourites(conn, docID, docType, emp) + "\" " +
						"url=\"Provider?type=edit&amp;element=task&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
						"><dbd>" + rs.getInt("DBD") + "</dbd>" + getViewContent(rs) + value);
				xmlContent.append("</entry>");	
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}


	public StringBuffer getTasksForReport(String condition, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, Set<DocID> toExpandResponses, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			if (!condition.trim().equals("")) condition = " and " + condition;
			String sql = "SELECT DISTINCT " + taskFields + " FROM TASKS t, READERS_TASKS rt WHERE t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" + 
					condition + " ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;
			/*String sql = "SELECT * FROM TASKS t WHERE EXISTS (SELECT DOCID FROM READERS_TASKS rt where t.DOCID = rt.DOCID and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" +
			condition + " ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;*/
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(1000);
				String viewText = rs.getString("VIEWTEXT");
                String id = rs.getString("DDBID");

				if (db.hasResponse(conn, docID, DOCTYPE_TASK, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
				}

				Statement se = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				sql = "SELECT userid, shortname, viewtext, responsible from TASKSEXECUTORS as te, EMPLOYERS as e " +
						" WHERE te.DOCID = " + docID + " AND te.executor = e.userid";

				value.append("<executors>");
				ResultSet rse = se.executeQuery(sql);
				while (rse.next()) {
					value.append("<entry userid=\"" + rse.getString("USERID") + "\" shortname=\"" +
							rse.getString("SHORTNAME") + "\""+ XMLUtil.getAsAttribute("viewtext", rse.getString("VIEWTEXT")) + 
							"responsible=\"" + rse.getInt("RESPONSIBLE") + "\">"+ "</entry>");
				}
				value.append("</executors>");
				for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value) != null ? rs.getString(field.value) : ""});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}

				if (toExpandResponses.size() > 0) {
					for (DocID doc : toExpandResponses) {
						if (doc.id == docID && doc.type == DOCTYPE_TASK) {
							DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
							value.append("<responses>" + responses.xmlContent + "</responses>");
						}
					}
				}
				xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + DOCTYPE_TASK + "\"  " +
						"docid=\"" + docID + "\" " +
						"id=\"" + id + "\" " +
                        XMLUtil.getAsAttribute("viewtext", viewText) +
						"url=\"Provider?type=edit&amp;element=document&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
						"><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>" + value);
				xmlContent.append("</entry>");
				se.close();
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}


	public int insertTask(Task doc, User user) {
		int key = 0;
		int id = doc.getDocID();
		Control ctrl = doc.getControl();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			Date viewDate = doc.getViewDate();
			  String sign = doc.getSign() != null ? String.format("'%s'", doc.getSign()) : "null";
			String fieldsAsText = "PARENTDOCID, PARENTDOCTYPE, TASKAUTHOR, AUTHOR, REGDATE, TASKVN, TASKDATE," +
					" CONTENT, COMMENT, CONTROLTYPE, CTRLDATE, DBD, ALLCONTROL, CYCLECONTROL, VIEWTEXT, " +
					" DOCTYPE, LASTUPDATE, FORM, TASKTYPE, ISOLD, HAR, PROJECT, CATEGORY, BRIEFCONTENT, " +
					" DEFAULTRULEID, " + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, HAS_ATTACHMENT, CUSTOMER, APPS, SIGN";
			String valuesAsText = doc.parentDocID + 
					", " + doc.parentDocType + 
					",'" + doc.getTaskAuthor() + 
					"', '" + doc.getAuthorID() + 
					"', '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + 
					"','" + doc.getTaskVn().trim() + 
					"','" + Database.sqlDateTimeFormat.format(doc.getTaskDate()) + 
					"', '" + doc.getContent().replace("'", "''") + 
					"', '" + doc.getComment().replace("'", "''") + 
				//	"', " + ctrl.getTypeID() + 
					", " + (ctrl.getExecDate() != null ? "'" + new Timestamp(ctrl.getExecDate().getTime()) + "'": "null") + 
					", " + doc.getDBD() + 
					", " + ctrl.getAllControl() + 
					", "	+ ctrl.getCycle() + 					
					",'" + doc.getViewText().replace("'", "''") + 
					"', " + Const.DOCTYPE_TASK + 
					", '" + Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + 					
					"','KR', " + doc.getResolTypeAsInt() + 
					", " + ctrl.getOld() + 
					", " + (doc.getHar() != 0 ? doc.getHar() : "null") + 
					"," + (doc.getProject() != 0 ? doc.getProject() : "null") + 
					"," + (doc.getCategory() != 0 ? doc.getCategory() : "null") + 
					",'" + doc.getBriefContent().replace("'", "''") + 
					"', '" + doc.getDefaultRuleID() + 
					"', '" + doc.getViewTextList().get(1).replace("'", "''") + 
					"', '" + doc.getViewTextList().get(2).replace("'", "''") + 
					"', '" + doc.getViewTextList().get(3).replace("'", "''") + 
					"', " + doc.getViewNumber() + 
					", " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'": "null") +
					", " + doc.blobFieldsMap.size() + ", " + doc.getCustomer() + ", " + doc.getApps() + ", " + sign;
			if (id != 0 && doc.hasField("recID")){
				fieldsAsText = "DOCID, " + fieldsAsText;
				valuesAsText = id + ", " + valuesAsText;
			}
			String sql = "insert into TASKS(" + fieldsAsText + ") values (" + valuesAsText + ")";
			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			} else {
				key = id;
			}
			for(Executor exec: doc.getExecutorsList()){
				String execSQL = "insert into TASKSEXECUTORS(DOCID, EXECUTOR, RESETDATE, RESETAUTHOR, COMMENT, RESPONSIBLE, EXECPERCENT) values ("
						+ key + ", '" + exec.getID() + "'," + exec.getResetDateAsDbFormat() + ", '" + exec.resetAuthorID + "','" + exec.comment.replace("'", "''") + "', " + exec.getResponsible() + ", " + exec.getPercentOfExecution() + ")";
				Statement statement = conn.createStatement();
				statement.executeUpdate(execSQL);
				statement.close();			
			}

			db.insertBlobTables(conn, id, key, doc, baseTable);
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
			DatabaseUtil.errorPrint(db.getDbID(),e);
			return -1;
		} catch (Exception e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
			return -1;
		} finally {			
			dbPool.returnConnection(conn);
		}
		return key;

	}


	protected int deleteExecutors(int taskKey, Connection conn) {
		String sql = "delete from TASKSEXECUTORS where RESETAUTHOR = '' and docid = ?";
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, taskKey);
			pst.executeUpdate();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
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
			DatabaseUtil.errorPrint(db.getDbID(),e);
		}
		return -1;
	}

	public int updateTask(Task doc, User user) throws DocumentAccessException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Task oldDoc = this.getTaskByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
			Control ctrl = doc.getControl();
			Connection conn = dbPool.getConnection();
			try {
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);
				this.deleteExecutors(doc.getDocID(), conn);
				Date viewDate = doc.getViewDate();
				  String sign = doc.getSign() != null ? String.format("'%s'", doc.getSign()) : "null";
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
						+ doc.getComment().replace("'", "''") + "'"
						//+, CONTROLTYPE="
				//		+ ctrl.getTypeID() + ", "
						+ ", CTRLDATE=" + (ctrl.getExecDate() != null ? "'" + new Timestamp(ctrl.getExecDate().getTime()) + "'": "null")
						+ ", DBD = " + doc.getDBD()
						+ ", HAR = " + (doc.getHar() != 0 ? doc.getHar() : "null")
						+ ", DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'"
						+ ", " + DatabaseUtil.getViewTextValues(doc)
						+ ", VIEWNUMBER = " + doc.getViewNumber()
						+ ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null") + ""
						+ ", PROJECT = " + (doc.getProject() != 0 ? doc.getProject() : "null")
						+ ", CATEGORY = " + (doc.getCategory() != 0 ? doc.getCategory() : "null")
						+ ", CYCLECONTROL=" + ctrl.getCycle() + ", TASKTYPE=" + doc.getResolTypeAsInt()
						+ ", ALLCONTROL=" + ctrl.getAllControl() + ", ISOLD=" + ctrl.getOld()
						+ ", LASTUPDATE = '"	+ Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + "'"
						+ ", APPS = " + doc.getApps()
						+ ", CUSTOMER = " + doc.getCustomer()
						+ ", VIEWTEXT='" + doc.getViewText() + "' "
                        + ", SIGN = " + sign
                        + " where DOCID=" + doc.getDocID();
				s.executeUpdate(update);
				
				//this.deleteExecutors(doc.getDocID(), conn);

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
				DatabaseUtil.errorPrint(db.getDbID(),e);
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

	public Task fillTaskData(ResultSet rs, Task task) {
		try {
			task.setTaskAuthor(rs.getString("TASKAUTHOR"));
			task.setTaskVn(rs.getString("TASKVN"));
			task.setTaskDate(rs.getTimestamp("TASKDATE"));
			task.setDBD(rs.getInt("DBD"));
			task.setContent(rs.getString("CONTENT"));
			task.setBriefContent(rs.getString("BRIEFCONTENT"));
			task.setComment(rs.getString("COMMENT"));
			task.setResolType(rs.getInt("TASKTYPE"));
			task.setHar(rs.getInt("HAR"));
			task.setProject(rs.getInt("PROJECT"));
			task.setCategory(rs.getInt("CATEGORY"));
			task.setDefaultRuleID(rs.getString("DEFAULTRULEID"));
			task.setCustomer(rs.getInt("CUSTOMER"));
			Control ctrl = new Control();
			task.setApps(rs.getInt("APPS"));

		//	ctrl.setType(rs.getInt("CONTROLTYPE"));
			ctrl.setPrimaryCtrlDate(rs.getTimestamp("CTRLDATE"));
			ctrl.setCycle(rs.getInt("CYCLECONTROL"));
			ctrl.setAllControl(rs.getInt("ALLCONTROL"));
			ctrl.setOld(rs.getInt("ISOLD"));
			task.setControl(ctrl);

			task.docType = Const.DOCTYPE_TASK;
			task.isValid = true;
			fillViewTextData(rs, task);
			fillSysData(rs, task);
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return task;
	}

	protected void insertAccessRelatedRec(String accessTableSuffix, int docID, Connection conn, Document doc) {
		try {
			String authorsTable = "AUTHORS_" + accessTableSuffix, authorsUpdateSQL = "";
			HashSet<String> authors = doc.getEditors();
			authors.add(Const.sysUser);
			authors.addAll(Arrays.asList(Const.observerGroup));
			for (String author : authors) {
				String hasAuthorSQL = "select count(*) from " + authorsTable
						+ " where DOCID=" + docID + " and USERNAME='" + author
						+ "'";
				ResultSet resultSet = conn.createStatement().executeQuery(
						hasAuthorSQL);

				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						authorsUpdateSQL += "('" + author + "', " + docID
								+ "),";
					}
				}
			}
			if (!authorsUpdateSQL.equals("")) {
				authorsUpdateSQL = authorsUpdateSQL.substring(0,
						authorsUpdateSQL.length() - 1);
				// System.out.println(authorsUpdateSQL);
				conn.prepareStatement(
						"insert into " + authorsTable
						+ "(USERNAME, DOCID) values "
						+ authorsUpdateSQL).executeUpdate();
			}

			String readersTable = "READERS_" + accessTableSuffix, readersUpdateSQL = "";
			HashSet<Reader> readers = doc.getReaders();
            readers.add(new Reader(Const.sysUser));

            for (String value : observerGroupAsList) {
                readers.add(new Reader(value));
            }
			for (Reader reader : readers) {
				String hasReaderSQL = "select count(*) from " + readersTable
						+ " where DOCID=" + docID + " and USERNAME='" + reader
						+ "'";
				ResultSet resultSet = conn.createStatement().executeQuery(
						hasReaderSQL);
				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						readersUpdateSQL += "('" + reader + "', " + docID
								+ "),";
					}
				}
			}
			if (!readersUpdateSQL.equals("")) {
				readersUpdateSQL = readersUpdateSQL.substring(0,
						readersUpdateSQL.length() - 1);
				conn.prepareStatement(
						"insert into " + readersTable
						+ "(USERNAME, DOCID) values "
						+ readersUpdateSQL).executeUpdate();
			}

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		}
	}

	@Override
	public int executorReset(Task doc, Set<String> complexUserID, String absoluteUserID, String executor, String resetAuthor) throws DocumentAccessException {
		return 0;
	}

	@Override
	public int recalculate() {	
		Connection conn = dbPool.getConnection();
		try {				
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			conn.setAutoCommit(false);	
			String update = "update tasks set dbd = DATEDIFF(DAY, now(), ctrldate) where allcontrol = 1";
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

	public  int recalculate(int docID) {
		Connection conn = dbPool.getConnection();
		try {				
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			conn.setAutoCommit(false);	
			String update = "update tasks set dbd = DATEDIFF(DAY, now(), ctrldate) where docid = " + docID + " and allcontrol = 1";
			String select = "select  dbd from tasks where docid ="+docID;
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

}
