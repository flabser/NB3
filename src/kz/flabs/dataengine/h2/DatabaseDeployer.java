package kz.flabs.dataengine.h2;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.alter.CheckDataBase;
import kz.flabs.dataengine.h2.ftengine.FTIndexEngineDeployer;
import kz.flabs.dataengine.h2.usersactivity.UsersActivityDDEScripts;
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


    public DatabaseDeployer(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        dbPool = new DBConnectionPool();
        this.env = env;
        dbPool.initConnectionPool(env.globalSetting.driver, env.globalSetting.dbURL);
    }

    public boolean deploy(){
        try {

            checkAndCreateTable(DDEScripts.getDBVersionTableDDE(), "DBVERSION");
            checkAndCreateTable(DDEScripts.getPatchesDDE(), "PATCHES");
            checkAndCreateTable(DDEScripts.getGlossaryDDE(), "GLOSSARY");
            checkAndCreateTable(DDEScripts.getGlossaryTreePath(), "GLOSSARY_TREE_PATH");
            checkAndCreateTable(DDEScripts.getCustomFieldGlossary(), "CUSTOM_FIELDS_GLOSSARY");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("GLOSSARY"), "CUSTOM_BLOBS_GLOSSARY")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("GLOSSARY"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("GLOSSARY"));
            }

            checkAndCreateTable(DDEScripts.getTopicsDDE(), "TOPICS");
            checkAndCreateTable(DDEScripts.getOrganizationDDE(), "ORGANIZATIONS");
            checkAndCreateTable(DDEScripts.getDepartmentDDE(), "DEPARTMENTS");
            if (checkAndCreateTable(DDEScripts.getEmployerDDE(), "EMPLOYERS")) {
                executeQuery(DDEScripts.getDepartmentsAlternationDDE());
            }

            checkAndCreateView(DDEScripts.getStructureView(), "STRUCTURE");
            checkAndCreateView(DDEScripts.getStructureCollectionView(), "STRUCTURECOLLECTION");
            checkAndCreateTable(DDEScripts.getStructureTreePath(), "STRUCTURE_TREE_PATH");
            checkAndCreateTable(DDEScripts.getFilterDDE(), "FILTER");

            checkAndCreateTable(DDEScripts.getMainDocsDDE(), "MAINDOCS");
            createTrigger(DDEScripts.getAddResponseTriggerDDE("MAINDOCS"));
            createTrigger(DDEScripts.getRemoveResponseTriggerDDE("MAINDOCS"));
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("MAINDOCS"), "CUSTOM_BLOBS_MAINDOCS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("MAINDOCS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("MAINDOCS"));
            }
            checkAndCreateAuthorsReadersTbl("AUTHORS_MAINDOCS", "MAINDOCS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_MAINDOCS", "MAINDOCS", "DOCID");
            if (checkAndCreateTable(DDEScripts.getCustomFieldsDDE(), "CUSTOM_FIELDS")){
                String indexDDE1 = DDEScripts.getIndexDDE("CUSTOM_FIELDS", "NAME");
                String indexDDE2 = DDEScripts.getIndexDDE("CUSTOM_FIELDS", "VALUE");
                createIndex(indexDDE1);
                createIndex(indexDDE2);
            }
            checkAndCreateTable(DDEScripts.getTasksDDE(), "TASKS");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("TASKS"), "CUSTOM_BLOBS_TASKS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("TASKS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("TASKS"));
            }
            checkAndCreateTable(DDEScripts.getTasksExecutorsDDE(), "TASKSEXECUTORS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_TASKS", "TASKS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_TASKS", "TASKS", "DOCID");
            checkAndCreateTable(DDEScripts.getProjecsDDE(), "PROJECTS");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("PROJECTS"), "CUSTOM_BLOBS_PROJECTS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("PROJECTS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("PROJECTS"));
            }
            checkAndCreateAuthorsReadersTbl("AUTHORS_PROJECTS", "PROJECTS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_PROJECTS", "PROJECTS", "DOCID");
            checkAndCreateTable(DDEScripts.getProjectRecipientsDDE(), "PROJECTRECIPIENTS");
            checkAndCreateTable(DDEScripts.getCoordBlockDDE(), "COORDBLOCKS");
            checkAndCreateTable(DDEScripts.getCoordinatorsDDE(), "COORDINATORS");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("COORDINATORS"), "CUSTOM_BLOBS_COORDINATORS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("COORDINATORS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("COORDINATORS"));
            }

            checkAndCreateTable(DDEScripts.getExecutionsDDE(), "EXECUTIONS");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("EXECUTIONS"), "CUSTOM_BLOBS_EXECUTIONS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("EXECUTIONS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("EXECUTIONS"));
            }
            checkAndCreateAuthorsReadersTbl("AUTHORS_EXECUTIONS", "EXECUTIONS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_EXECUTIONS", "EXECUTIONS", "DOCID");
            checkAndCreateTable(DDEScripts.getCountersTableDDE(), "COUNTERS");
            checkAndCreateTable(DDEScripts.getGlossaryDDE(), "GLOSSARY");
            checkAndCreateTable(DDEScripts.getCustomFieldGlossary(), "CUSTOM_FIELDS_GLOSSARY");


            checkAndCreateAuthorsReadersTbl("AUTHORS_TOPICS", "TOPICS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_TOPICS", "TOPICS", "DOCID");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("TOPICS"), "CUSTOM_BLOBS_TOPICS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("TOPICS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("TOPICS"));
            }



            checkAndCreateTable(DDEScripts.getPostsDDE(), "POSTS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_POSTS", "POSTS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_POSTS", "POSTS", "DOCID");
            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDE("POSTS"), "CUSTOM_BLOBS_POSTS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("POSTS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("POSTS"));
            }

            checkAndCreateTable(DDEScripts.getForumTreePath(), "FORUM_TREE_PATH");
            checkAndCreateTable(DDEScripts.getUserRolesDDE(), "USER_ROLES");

            checkAndCreateTable(DDEScripts.getGroupsDDE(), "GROUPS");
            checkAndCreateTable(DDEScripts.getUserGroupsDDE(), "USER_GROUPS");

            if (checkAndCreateTable(DDEScripts.getCustomBlobsDDEForStruct("EMPLOYERS"), "CUSTOM_BLOBS_EMPLOYERS")) {
                createTrigger(DDEScripts.getAddAttachmentTriggerDDE("EMPLOYERS"));
                createTrigger(DDEScripts.getRemoveAttachmentTriggerDDE("EMPLOYERS"));
            }

            checkAndCreateTable(DDEScripts.getConditionDDE(), "CONDITION");

            checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityDDE(), "USERS_ACTIVITY");
            checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityChangesDDE(), "USERS_ACTIVITY_CHANGES");
            checkAndCreateTable(UsersActivityDDEScripts.getRecycleBinDDE(), "RECYCLE_BIN");
            checkAndCreateTable(DDEScripts.getSyncDDE(), "SYNC");
            checkAndCreateTable(UsersActivityDDEScripts.getActivityDDE(), "ACTIVITY");
        }catch (Throwable e)  {
            DatabaseUtil.debugErrorPrint(e);
        }

        try{
            AppEnv.logger.normalLogEntry("FT-index initilaize ... ");
            FTIndexEngineDeployer searchEngine = new FTIndexEngineDeployer(dbPool);
            searchEngine.initEngine();
            searchEngine.createFTIndex("MAINDOCS", null);
            searchEngine.createFTIndex("CUSTOM_FIELDS", "VALUE,VALUEASDATE,VALUEASNUMBER,VALUEASCLOB");
            searchEngine.createFTIndex("TASKS", null);
            searchEngine.createFTIndex("EXECUTIONS", null);
            searchEngine.createFTIndex("PROJECTS", null);
        }catch (Throwable e)  {
            DatabaseUtil.debugErrorPrint(e);
        }

        try{
            CheckDataBase checker = new CheckDataBase(env, false);
            if(checker.check()){
                deployed = true;
            }
        }catch (Throwable e)  {
            DatabaseUtil.debugErrorPrint(e);
        }
        return deployed;
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
                    AppEnv.logger.errorLogEntry("Table" + tableName + " has not created");
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
                    AppEnv.logger.errorLogEntry("Access table related with " + tableName + " has not created");
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
                            _Session session = new _Session(env, new User(Const.sysUser),this);
                            myObject.setSession(session);
                            myObject.setConnection(scriptConn);
                            String resObj = myObject.patchHandlerProcess();
                            executeQuery("insert into patches (PROCESSEDTIME, HASH, DESCRIPTION, NAME) VALUES ('" + new Timestamp(new Date().getTime()) + "', " + handler.hashCode() + ", " + "'" + handler.description + "', '" + handler.id + "')");
                            //	System.out.println(resObj);
                        } catch (Exception e) {
                            ScriptProcessor.logger.errorLogEntry(script);
                            ScriptProcessor.logger.errorLogEntry(e);
                            executeQuery("insert into patches (PROCESSEDTIME, HASH, DESCRIPTION, NAME) VALUES ('" + new Timestamp(new Date().getTime()) + "', " + handler.hashCode() + ", " + "'" + handler.description + " " + e + "', '" + handler.id + "')");
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
