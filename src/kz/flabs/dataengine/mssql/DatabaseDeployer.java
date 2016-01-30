package kz.flabs.dataengine.mssql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.DBConnectionPool;
import kz.flabs.dataengine.mssql.alter.CheckDataBase;
import kz.flabs.dataengine.mssql.useractivity.UsersActivityDDEScripts;
import kz.flabs.exception.RuleException;
import kz.flabs.scriptprocessor.IScriptSource;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.handler.TriggerType;
import kz.nextbase.script._Session;
import kz.pchelka.scheduler.IProcessInitiator;

import java.sql.*;
import java.util.Collection;
import java.util.Date;

public class DatabaseDeployer implements IDatabaseDeployer, IProcessInitiator {
	public boolean deployed;

	private AppEnv env;
	private IDBConnectionPool dbPool;
	private String connectionURL = "";


	public DatabaseDeployer(AppEnv env) throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {	
		this.env = env;
		connectionURL = env.globalSetting.dbURL;
		dbPool = new DBConnectionPool();	
		dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(), env.globalSetting.getDbPassword());	

	}

	public boolean deploy(){		
		try {

			checkAndCreateTable(DDEScripts.getDBVersionTableDDE(), "DBVERSION");
			checkAndCreateTable(DDEScripts.getPatchesDDE(), "PATCHES");
			//checkAndCreateFunction(DDEScripts.getUpdateFTSTSVectorFunctionDDE());

			
			checkAndCreateTable(DDEScripts.getTopicsDDE(), "TOPICS");
			checkAndCreateAuthorsReadersTbl("AUTHORS_TOPICS", "TOPICS", "DOCID");			
			checkAndCreateAuthorsReadersTbl("READERS_TOPICS", "TOPICS", "DOCID");
			checkAndCreateTable(DDEScripts.getCustomBlobsDDE("TOPICS"), "CUSTOM_BLOBS_TOPICS"); 
			
			
			checkAndCreateTable(DDEScripts.getPostsDDE(), "POSTS");
			checkAndCreateAuthorsReadersTbl("AUTHORS_POSTS", "POSTS", "DOCID");
			checkAndCreateAuthorsReadersTbl("READERS_POSTS", "POSTS", "DOCID");
			checkAndCreateTable(DDEScripts.getCustomBlobsDDE("POSTS"), "CUSTOM_BLOBS_POSTS");	

			checkAndCreateTable(DDEScripts.getGlossaryDDE(), "GLOSSARY");
			checkAndCreateTable(DDEScripts.getGlossaryTreePath(), "GLOSSARY_TREE_PATH");
			checkAndCreateTable(DDEScripts.getCustomFieldGlossary(), "CUSTOM_FIELDS_GLOSSARY");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("GLOSSARY"), "CUSTOM_BLOBS_GLOSSARY");
			checkAndCreateTable(DDEScripts.getMainDocsDDE(), "MAINDOCS");
			checkAndCreateTable(DDEScripts.getCustomBlobsDDE("MAINDOCS"), "CUSTOM_BLOBS_MAINDOCS");
			checkAndCreateAuthorsReadersTbl("AUTHORS_MAINDOCS", "MAINDOCS", "DOCID");
			checkAndCreateAuthorsReadersTbl("READERS_MAINDOCS", "MAINDOCS", "DOCID");
			if (checkAndCreateTable(DDEScripts.getCustomFieldsDDE(), "CUSTOM_FIELDS")){
				String indexDDE1 = DDEScripts.getIndexDDE("CUSTOM_FIELDS", "NAME");
				String indexDDE2 = DDEScripts.getIndexDDE("CUSTOM_FIELDS", "VALUE");
				createIndex(indexDDE1);
				createIndex(indexDDE2);
			}		
			checkAndCreateTable(DDEScripts.getTasksDDE(), "TASKS");
			checkAndCreateTable(DDEScripts.getCustomBlobsDDE("TASKS"), "CUSTOM_BLOBS_TASKS");
			checkAndCreateTable(DDEScripts.getTasksExecutorsDDE(), "TASKSEXECUTORS");
			checkAndCreateAuthorsReadersTbl("AUTHORS_TASKS", "TASKS", "DOCID");
			checkAndCreateAuthorsReadersTbl("READERS_TASKS", "TASKS", "DOCID");
			checkAndCreateTable(DDEScripts.getProjecsDDE(), "PROJECTS");
			checkAndCreateTable(DDEScripts.getCustomBlobsDDE("PROJECTS"), "CUSTOM_BLOBS_PROJECTS");
			checkAndCreateAuthorsReadersTbl("AUTHORS_PROJECTS", "PROJECTS", "DOCID");
			checkAndCreateAuthorsReadersTbl("READERS_PROJECTS", "PROJECTS", "DOCID");
			checkAndCreateTable(DDEScripts.getProjectRecipientsDDE(), "PROJECTRECIPIENTS");
			checkAndCreateTable(DDEScripts.getCoordBlockDDE(), "COORDBLOCKS");
			checkAndCreateTable(DDEScripts.getCoordinatorsDDE(), "COORDINATORS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("COORDINATORS"), "CUSTOM_BLOBS_COORDINATORS");

			checkAndCreateTable(DDEScripts.getExecutionsDDE(), "EXECUTIONS");
			checkAndCreateTable(DDEScripts.getCustomBlobsDDE("EXECUTIONS"), "CUSTOM_BLOBS_EXECUTIONS");
			checkAndCreateAuthorsReadersTbl("AUTHORS_EXECUTIONS", "EXECUTIONS", "DOCID");
			checkAndCreateAuthorsReadersTbl("READERS_EXECUTIONS", "EXECUTIONS", "DOCID");
			checkAndCreateTable(DDEScripts.getCountersTableDDE(), "COUNTERS");
			checkAndCreateTable(DDEScripts.getGlossaryDDE(), "GLOSSARY");
			checkAndCreateTable(DDEScripts.getCustomFieldGlossary(), "CUSTOM_FIELDS_GLOSSARY");

			
			
			checkAndCreateTable(DDEScripts.getForumTreePath(), "FORUM_TREE_PATH");
			
			checkAndCreateTable(DDEScripts.getOrganizationDDE(), "ORGANIZATIONS");
			checkAndCreateTable(DDEScripts.getDepartmentDDE(), "DEPARTMENTS");
			//checkAndCreateTrigger(DDEScripts.getDepartmentsDelCascadeTrigger(), "DEPDELCASCADETRIGGER");
			if (checkAndCreateTable(DDEScripts.getEmployerDDE(), "EMPLOYERS")) {
				executeQuery(DDEScripts.getDepartmentsAlternationDDE());
			}
            checkAndCreateView(kz.flabs.dataengine.mssql.DDEScripts.getStructureView(), "STRUCTURE");
            checkAndCreateView(kz.flabs.dataengine.mssql.DDEScripts.getStructureCollectionView(), "STRUCTURECOLLECTION");
            checkAndCreateTable(kz.flabs.dataengine.mssql.DDEScripts.getStructureTreePath(), "STRUCTURE_TREE_PATH");
			//checkAndCreateTrigger(DDEScripts.getEmployersDelCascadeTrigger(), "EMPDELCASCADETRIGGER");
			//checkAndCreateTable(DDEScripts.getRolesDDE(), "ROLES");
			checkAndCreateTable(DDEScripts.getUserRolesDDE(), "USER_ROLES");

			checkAndCreateTable(DDEScripts.getGroupsDDE(), "GROUPS");
			checkAndCreateTable(DDEScripts.getUserGroupsDDE(), "USER_GROUPS");

			checkAndCreateTable(DDEScripts.getCustomBlobsDDEForStruct("EMPLOYERS"), "CUSTOM_BLOBS_EMPLOYERS");

			checkAndCreateTable(DDEScripts.getQueueDDE(), "QUEUE");

			checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityDDE(), "USERS_ACTIVITY");
			checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityChangesDDE(), "USERS_ACTIVITY_CHANGES");
			checkAndCreateTable(UsersActivityDDEScripts.getRecycleBinDDE(), "RECYCLE_BIN");
			checkAndCreateTable(DDEScripts.getSyncDDE(), "SYNC");

			executeQuery(DDEScripts.delResponsesTriggerDDE("MAINDOCS"));
			executeQuery(DDEScripts.delAttachmentTriggerDDE(Const.DOCTYPE_MAIN));
			executeQuery(DDEScripts.delAttachmentTriggerDDE(Const.DOCTYPE_TASK));
			executeQuery(DDEScripts.delAttachmentTriggerDDE(Const.DOCTYPE_EXECUTION));
			executeQuery(DDEScripts.delAttachmentTriggerDDE(Const.DOCTYPE_PROJECT));
			executeQuery(DDEScripts.delAttachmentTriggerDDE(Const.DOCTYPE_EMPLOYER));

			createTrigger(DDEScripts.getResponsesTriggerDDE("MAINDOCS"));
			createTrigger(DDEScripts.getAttachmentTriggerDDE(Const.DOCTYPE_MAIN));
			createTrigger(DDEScripts.getAttachmentTriggerDDE(Const.DOCTYPE_TASK));
			createTrigger(DDEScripts.getAttachmentTriggerDDE(Const.DOCTYPE_EXECUTION));
			createTrigger(DDEScripts.getAttachmentTriggerDDE(Const.DOCTYPE_PROJECT));
			createTrigger(DDEScripts.getAttachmentTriggerDDE(Const.DOCTYPE_EMPLOYER));
			
			checkAndCreateFTCatalog("ft", DDEScripts.createFullTextCatalog());
			checkAndCreateIndex(Const.DOCTYPE_MAIN, DDEScripts.createFullTextIndex(Const.DOCTYPE_MAIN));
			checkAndCreateIndex("CUSTOM_FIELDS", DDEScripts.createFullTextIndex("CUSTOM_FIELDS"));
			checkAndCreateIndex(Const.DOCTYPE_TASK, DDEScripts.createFullTextIndex(Const.DOCTYPE_TASK));
			checkAndCreateIndex(Const.DOCTYPE_EXECUTION, DDEScripts.createFullTextIndex(Const.DOCTYPE_EXECUTION));
			checkAndCreateIndex(Const.DOCTYPE_PROJECT, DDEScripts.createFullTextIndex(Const.DOCTYPE_PROJECT));
			checkAndCreateTable(DDEScripts.getFilterDDE(), "FILTER");
			checkAndCreateTable(DDEScripts.getConditionDDE(), "CONDITION");
			CheckDataBase checker = new CheckDataBase(env); 
			if(checker.check()){
				deployed = true;
			}
		}catch (Throwable e)  {   
			DatabaseUtil.debugErrorPrint(e);
		}
		return deployed;
	}

	public boolean checkAndCreateFunction(String sqlExpression) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			if (!s.execute(sqlExpression)) {
				conn.commit();
				s.close();
				return true;
			} else {
				AppEnv.logger.errorLogEntry("error 5688");
			}
			conn.commit();
			s.close();
		}catch(Throwable e){
			DatabaseUtil.debugErrorPrint(e);
		} finally {	
			dbPool.returnConnection(conn);	
		}
		return false;
	}

    public boolean checkAndCreateView(String scriptCreateView, String viewName){
        Connection conn = dbPool.getConnection();
        try{
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            if(!DatabaseUtil.hasView(viewName, conn)){
                if(!s.execute(scriptCreateView)){
                    conn.commit();
                    s.close();
                    return true;
                }else{
                    AppEnv.logger.errorLogEntry("View" + viewName + " has not created");
                }
            }
            conn.commit();
            s.close();
        }catch(Throwable e){
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return false;
    }

	public boolean checkAndCreateTable(String scriptCreateTable, String tableName){
		Connection conn = dbPool.getConnection();
		try{
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			if(!DatabaseUtil.hasTable(tableName, conn)){
				if(!s.execute(scriptCreateTable)){									
					conn.commit();
					s.close();
					return true;
				}else{
					AppEnv.logger.errorLogEntry("Table " + tableName + " has not created");
				}
			}			
			s.close();
		}catch(Throwable e){
			DatabaseUtil.debugErrorPrint(e);
		} finally {	
			dbPool.returnConnection(conn);	
		}
		return false;
	}

	public boolean checkAndCreateTrigger(String scriptCreateTrigger, String triggerName){
		Connection conn = dbPool.getConnection();
		try{
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			if(!DatabaseUtil.hasTrigger(triggerName, conn)){
				if(!s.execute(scriptCreateTrigger)){									
					conn.commit();
					s.close();
					return true;
				}else{
					AppEnv.logger.errorLogEntry("Trigger " + triggerName + " has note created");
				}
			}			
			s.close();
		}catch(Throwable e){
			DatabaseUtil.debugErrorPrint(e);
		} finally {	
			dbPool.returnConnection(conn);	
		}
		return false;
	}

	// fts BEFORE INSERT OR UPDATE trigger
	public boolean checkAndCreateTriggerFTS(String mainTableName) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			s.addBatch("DROP TRIGGER IF EXISTS trigger_update_" + mainTableName + "_fts ON " + mainTableName + ";");
			s.addBatch("CREATE TRIGGER trigger_update_" + mainTableName + "_fts " +
					" BEFORE INSERT OR UPDATE ON " + mainTableName +
					" FOR EACH ROW EXECUTE PROCEDURE update_fts_tsvector();");
			s.executeBatch();
			s.close();
			conn.commit();
			return true;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public boolean createTrigger(String triggerDDE) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			s.execute(triggerDDE);
			s.close();
			conn.commit();
			return true;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public boolean executeQuery(String query) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			s.execute(query);
			s.close();
			conn.commit();
			return true;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public void checkAndCreateIndex(int docType, String indexDDE) {
		Connection conn = dbPool.getConnection();
		String tableName = DatabaseUtil.getMainTableName(docType);
		try {
			conn.setAutoCommit(false);
			if (!DatabaseUtil.hasFTIndex(conn, tableName)) {				
				conn.commit();
				PreparedStatement pst = conn.prepareStatement(indexDDE);
				pst.execute();
				pst.close();
			}
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
	
	public void checkAndCreateIndex(String tableName, String indexDDE) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			if (!DatabaseUtil.hasFTIndex(conn, tableName)) {				
				conn.commit();
				PreparedStatement pst = conn.prepareStatement(indexDDE);
				pst.execute();
				pst.close();
			}
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
	
	public void checkAndCreateFTCatalog(String catalogName, String catalogDDE) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			if (!DatabaseUtil.hasFTCatalog(conn, catalogName)) {				
				conn.commit();
				PreparedStatement pst = conn.prepareStatement(catalogDDE);
				pst.execute();
				pst.close();
			}
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
	
	public boolean createIndex(String indexDDE){
		Connection conn = dbPool.getConnection();
		try{
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			s.execute(indexDDE);									
			s.close();
			conn.commit();
			return true;
		}catch(Throwable e){
			DatabaseUtil.debugErrorPrint(e);
		}finally{	
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public void checkAndCreateProsedure(String scriptCreateTable, String procedurName){
		Connection conn = dbPool.getConnection();
		try{
			conn.setAutoCommit(false);
			if(!DatabaseUtil.hasProcedureAndTriger(procedurName, conn)){
				PreparedStatement pst = conn.prepareStatement(scriptCreateTable);
				pst.execute();
				pst.close();
			}
			conn.commit();
		}catch(SQLException e){
			DatabaseUtil.debugErrorPrint(e);
		} finally {	
			dbPool.returnConnection(conn);			
		}
	}



	public void checkAndCreateAuthorsReadersTbl(String tableName, String mainTable, String docID) throws SQLException{		
		Connection conn = dbPool.getConnection();
		try{		 
			conn.setAutoCommit(false);
			if(!DatabaseUtil.hasTable(tableName, conn)){			
				Statement s = conn.createStatement();
				if (!s.execute(DDEScripts.getAuthorReadersDDE(tableName, mainTable, docID))){
					String indexDDE = DDEScripts.getIndexDDE(tableName, "USERNAME");
					Statement si = conn.createStatement();
					si.execute(indexDDE);
					si.close();
				}else{
					AppEnv.logger.errorLogEntry("error 3432");
				}			
				s.close();
			}
			conn.commit();
		}catch(Throwable e){
			DatabaseUtil.debugErrorPrint(e);
		} finally {	
			dbPool.returnConnection(conn);		
		}
	}



	/*public boolean getDBVersion(){
		try{
			DatabaseUtil.getDBVersion(connectionURL, sysDbVersion, appDbVersion);
		}

		return false; 
	}*/


    public void testInsert() throws SQLException {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            String sql = "insert into MAINDOCS(AUTHOR, REGDATE, DOCTYPE)values('Erke', '1960-01-01 23:03:20', 8)";
            s.execute(sql);
            ResultSet rs = s.executeQuery("select * from MAINDOCS");
            while (rs.next()) {
                System.out.println(rs.getString("AUTHOR"));
                System.out.println(rs.getString("REGDATE"));
                System.out.println(rs.getString("DOCTYPE"));

            }
            conn.commit();
            rs.close();
            s.close();
            conn.close();
        } finally {
            dbPool.returnConnection(conn);
        }
    }

	public static void main(String[]args) throws SQLException{
		//DatabaseDeployer dd=new DatabaseDeployer("test");
		//dd.deploy();
		//dd.testInsert();
	}

    public boolean hasTable(String tableName) throws SQLException {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            String query = "select * from " + tableName;
            s.executeQuery(query);
            s.close();
        } catch (Throwable e) {
            return false;
        } finally {
            dbPool.returnConnection(conn);
        }
        return true;
    }

	public boolean patch() {		
		Connection conn = dbPool.getConnection();
		WebRuleProvider wrp = env.ruleProvider;			
		try {
			Collection<HandlerRule> handlers = wrp.getHandlerRules(true);
			Statement stat = conn.createStatement();
			String query = ""; 
			String script = "";
			for (HandlerRule handler : handlers) {
				if (handler.trigger == TriggerType.PATCH) {
					query = "SELECT * FROM PATCHES WHERE HASH = " + handler.hashCode();
					ResultSet rs = stat.executeQuery(query);
					if (!rs.next()) {
						Connection scriptConn = dbPool.getConnection();
						try{
							script = handler.getScript();
							ScriptProcessor sp = new ScriptProcessor();
							IScriptSource myObject = sp.setScriptLauncher(script, false);
							User u = new User(Const.sysUser);
							_Session session = new _Session(env, u, this);
							myObject.setSession(session);							
							myObject.setConnection(scriptConn);
							String resObj = myObject.patchHandlerProcess();
							String sql = "insert into patches (PROCESSEDTIME, HASH, DESCRIPTION, NAME) VALUES (" + new Timestamp(new Date().getTime()) + ", " + handler.hashCode() + ", " + "'" + handler.description + "', '" + handler.id + "')";
							executeQuery("insert into patches (PROCESSEDTIME, HASH, DESCRIPTION, NAME) VALUES ('" + new Timestamp(new Date().getTime()) + "', " + handler.hashCode() + ", " + "'" + handler.description + "', '" + handler.id + "')");
							conn.commit();
							rs.close();
							System.out.println(resObj);
						} catch (Exception e) {
							ScriptProcessor.logger.errorLogEntry(script);
							ScriptProcessor.logger.errorLogEntry(e);
							return false;
						} finally {
							dbPool.returnConnection(scriptConn);
						}
					}
				}
			}
			stat.close();
		} catch (RuleException e) {
			DatabaseUtil.debugErrorPrint(e);
			return false;
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}
	
	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}
}
