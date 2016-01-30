package kz.flabs.dataengine.postgresql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.dataengine.h2.DBConnectionPool;
import kz.flabs.dataengine.postgresql.alter.CheckDataBase;
import kz.flabs.dataengine.postgresql.useractivity.UsersActivityDDEScripts;
import kz.flabs.exception.RuleException;
import kz.flabs.scriptprocessor.IScriptSource;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.handler.TriggerType;
import kz.nextbase.script._Session;
import kz.pchelka.scheduler.IProcessInitiator;

public class DatabaseDeployer implements IDatabaseDeployer, IProcessInitiator {
    public boolean deployed;

    private AppEnv env;
    private IDBConnectionPool dbPool;
    private String connectionURL = "";

    public DatabaseDeployer(AppEnv env)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {
        this.env = env;
        connectionURL = env.globalSetting.dbURL;
        dbPool = new DBConnectionPool();
        dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(),
                env.globalSetting.getDbPassword());

    }

    @Override
    public boolean deploy() {
        try {

            checkAndCreateTable(DDEScripts.getDBVersionTableDDE(), "DBVERSION");
            checkAndCreateTable(DDEScripts.getPatchesDDE(), "PATCHES");
            checkAndCreateFunction(DDEScripts.getCountAttachmentFunctionDDE());
            checkAndCreateFunction(DDEScripts.getCountResponsesFunctionDDE());
            checkAndCreateFunction(DDEScripts.getUpdateFTSTSVectorFunctionDDE());

            checkAndCreateTable(DDEScripts.getGlossaryDDE(), "GLOSSARY");
            checkAndCreateTable(DDEScripts.getGlossaryTreePath(), "GLOSSARY_TREE_PATH");
            checkAndCreateTable(DDEScripts.getCustomFieldGlossary(), "CUSTOM_FIELDS_GLOSSARY");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("GLOSSARY"), "CUSTOM_BLOBS_GLOSSARY");

            checkAndCreateTable(DDEScripts.getCountersTableDDE(), "COUNTERS");

            checkAndCreateTable(DDEScripts.getTopicsDDE(), "TOPICS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_TOPICS", "TOPICS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_TOPICS", "TOPICS", "DOCID");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("TOPICS"), "CUSTOM_BLOBS_TOPICS");
            checkAndCreateTable(DDEScripts.getMainDocsDDE(), "MAINDOCS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("MAINDOCS"), "CUSTOM_BLOBS_MAINDOCS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_MAINDOCS", "MAINDOCS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_MAINDOCS", "MAINDOCS", "DOCID");
            if (checkAndCreateTable(DDEScripts.getCustomFieldsDDE(), "CUSTOM_FIELDS")) {
                String indexDDE1 = DDEScripts.getIndexDDE("CUSTOM_FIELDS", "NAME");
                String indexDDE2 = DDEScripts.getIndexDDE("CUSTOM_FIELDS", "VALUE");
                createIndex(indexDDE1);
                createIndex(indexDDE2);
            }
            checkAndCreateTable(DDEScripts.getProjecsDDE(), "PROJECTS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("PROJECTS"), "CUSTOM_BLOBS_PROJECTS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_PROJECTS", "PROJECTS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_PROJECTS", "PROJECTS", "DOCID");
            checkAndCreateTable(DDEScripts.getProjectRecipientsDDE(), "PROJECTRECIPIENTS");
            checkAndCreateTable(DDEScripts.getCoordBlockDDE(), "COORDBLOCKS");
            checkAndCreateTable(DDEScripts.getCoordinatorsDDE(), "COORDINATORS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("COORDINATORS"), "CUSTOM_BLOBS_COORDINATORS");
            checkAndCreateTable(DDEScripts.getTasksDDE(), "TASKS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("TASKS"), "CUSTOM_BLOBS_TASKS");
            checkAndCreateTable(DDEScripts.getTasksExecutorsDDE(), "TASKSEXECUTORS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_TASKS", "TASKS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_TASKS", "TASKS", "DOCID");
            checkAndCreateTable(DDEScripts.getExecutionsDDE(), "EXECUTIONS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("EXECUTIONS"), "CUSTOM_BLOBS_EXECUTIONS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_EXECUTIONS", "EXECUTIONS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_EXECUTIONS", "EXECUTIONS", "DOCID");

            checkAndCreateTable(DDEScripts.getPostsDDE(), "POSTS");
            checkAndCreateAuthorsReadersTbl("AUTHORS_POSTS", "POSTS", "DOCID");
            checkAndCreateAuthorsReadersTbl("READERS_POSTS", "POSTS", "DOCID");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDE("POSTS"), "CUSTOM_BLOBS_POSTS");

            checkAndCreateTable(DDEScripts.getForumTreePath(), "FORUM_TREE_PATH");

            checkAndCreateTable(DDEScripts.getOrganizationDDE(), "ORGANIZATIONS");
            checkAndCreateTable(DDEScripts.getDepartmentDDE(), "DEPARTMENTS");
            if (checkAndCreateTable(DDEScripts.getEmployerDDE(), "EMPLOYERS")) {
                executeQuery(DDEScripts.getDepartmentsAlternationDDE());
            }

            checkAndCreateView(DDEScripts.getStructureView(), "STRUCTURE");
            checkAndCreateView(DDEScripts.getStructureCollectionView(), "STRUCTURECOLLECTION");
            checkAndCreateTable(DDEScripts.getStructureTreePath(), "STRUCTURE_TREE_PATH");

            checkAndCreateTable(DDEScripts.getUserRolesDDE(), "USER_ROLES");
            checkAndCreateTable(DDEScripts.getGroupsDDE(), "GROUPS");
            checkAndCreateTable(DDEScripts.getUserGroupsDDE(), "USER_GROUPS");
            checkAndCreateTable(DDEScripts.getCustomBlobsDDEForStruct("EMPLOYERS"), "CUSTOM_BLOBS_EMPLOYERS");
            checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityDDE(), "USERS_ACTIVITY");
            checkAndCreateTable(UsersActivityDDEScripts.getUsersActivityChangesDDE(), "USERS_ACTIVITY_CHANGES");
            checkAndCreateTable(UsersActivityDDEScripts.getActivityDDE(), "ACTIVITY");
            checkAndCreateTable(UsersActivityDDEScripts.getRecycleBinDDE(), "RECYCLE_BIN");

            checkAndCreateIndex("maindocs", "maindocs_fts_idx");
            checkAndCreateIndex("custom_fields", "custom_fields_fts_idx");
            checkAndCreateIndex("tasks", "tasks_fts_idx");
            checkAndCreateIndex("executions", "executions_fts_idx");
            checkAndCreateIndex("projects", "projects_fts_idx");

            checkAndCreateTrigger("MAINDOCS");
            checkAndCreateResponseTrigger("MAINDOCS");
            checkAndCreateTrigger("TASKS");
            checkAndCreateTrigger("EXECUTIONS");
            checkAndCreateTrigger("PROJECTS");
            checkAndCreateTrigger("EMPLOYERS");

            checkAndCreateTriggerFTS("MAINDOCS");
            checkAndCreateTriggerFTS("CUSTOM_FIELDS");
            checkAndCreateTriggerFTS("TASKS");
            checkAndCreateTriggerFTS("EXECUTIONS");
            checkAndCreateTriggerFTS("PROJECTS");
            checkAndCreateTable(DDEScripts.getFilterDDE(), "FILTER");
            checkAndCreateTable(DDEScripts.getConditionDDE(), "CONDITION");

            String dbVersion = dbPool.getDatabaseVersion();
            dbVersion = dbVersion.substring(dbVersion.indexOf(" ") + 1, dbVersion.indexOf(","));
            /*if (dbVersion.compareTo("9.3") >= 0) {
                checkAndCreateMaterializedView(DDEScripts.getForAcquaintMaterializedViewDDE(), "FORACQUAINT");
                checkAndCreateFunction(DDEScripts.getForAcquaintFunctionDDE());
                checkAndCreateNamedTrigger(DDEScripts.getForAcquaintTriggerDDE(), "UPDATE_VIEW_FOR_ACQUAINT",
                        "USERS_ACTIVITY");
            } else {
                checkAndCreateMaterializedView(DDEScripts.getForAcquaintViewDDE(), "FORACQUAINT");
            }
            if (dbVersion.compareTo("9.1") > 0) {
                checkAndCreateMaterializedView(DDEScripts.getForAcquaintViewIndexDDE(), "FORACQUAINT");
            }
            checkAndCreateMaterializedView(DDEScripts.getForAcquaintViewDDE(), "FORACQUAINT");
            */checkAndCreateFunction(DDEScripts.getDiscussionFlagFunction());
            checkAndCreateNamedTrigger(DDEScripts.getDiscussionFlagTrigger(), "SET_REMOVE_DISCUSSION_FLAG_MAINDOCS",
                    "TOPICS");
            CheckDataBase checker = new CheckDataBase(env);

            if (checker.check()) {
                deployed = true;
            }
        } catch (Throwable e) {
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
                AppEnv.logger.errorLogEntry("error 1234");
            }
            conn.commit();
            s.close();
        } catch (Throwable e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return false;
    }

    public boolean checkAndCreateTable(String scriptCreateTable, String tableName) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            if (!DatabaseUtil.hasTable(tableName, conn)) {
                if (!s.execute(scriptCreateTable)) {
                    conn.commit();
                    s.close();
                    return true;
                } else {
                    AppEnv.logger.errorLogEntry("error 72169");
                }
            }
            s.close();
        } catch (Throwable e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return false;
    }

    public boolean checkAndCreateTrigger(String mainTableName) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            s.addBatch("DROP TRIGGER IF EXISTS set_remove_att_flag_" + mainTableName + " ON CUSTOM_BLOBS_"
                    + mainTableName);
            s.addBatch(DDEScripts.getAttachmentTriggerDDE(mainTableName));
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

    public boolean checkAndCreateNamedTrigger(String scriptCreateTrigger, String triggerName, String mainTableName) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            s.addBatch("DROP TRIGGER IF EXISTS " + triggerName + " ON " + mainTableName);
            s.addBatch(scriptCreateTrigger);
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

    public boolean checkAndCreateResponseTrigger(String mainTableName) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            s.addBatch("DROP TRIGGER IF EXISTS set_remove_resp_flag_" + mainTableName + " ON " + mainTableName);
            s.addBatch(DDEScripts.getResponsesTriggerDDE(mainTableName));
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

    // fts BEFORE INSERT OR UPDATE trigger
    public boolean checkAndCreateTriggerFTS(String mainTableName) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            s.addBatch("DROP TRIGGER IF EXISTS trigger_update_" + mainTableName + "_fts ON " + mainTableName + ";");
            s.addBatch("CREATE TRIGGER trigger_update_" + mainTableName + "_fts " + " BEFORE INSERT OR UPDATE ON "
                    + mainTableName + " FOR EACH ROW EXECUTE PROCEDURE update_fts_tsvector();");
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

    public boolean createIndex(String indexDDE) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            s.execute(indexDDE);
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

    public void checkAndCreateProsedure(String scriptCreateTable, String procedurName) {
        Connection conn = dbPool.getConnection();
        try {
            if (!DatabaseUtil.hasProcedureAndTriger(procedurName, conn)) {
                PreparedStatement pst = conn.prepareStatement(scriptCreateTable);
                pst.execute();
                pst.close();
            }
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
    }

    public void checkAndCreateAuthorsReadersTbl(String tableName, String mainTable, String docID) throws SQLException {
        Connection conn = dbPool.getConnection();
        try {
            if (!DatabaseUtil.hasTable(tableName, conn)) {
                Statement s = conn.createStatement();
                if (!s.execute(DDEScripts.getAuthorReadersDDE(tableName, mainTable, docID))) {
                    String indexDDE = DDEScripts.getIndexDDE(tableName, "USERNAME");
                    Statement si = conn.createStatement();
                    si.execute(indexDDE);
                    si.close();
                } else {
                    AppEnv.logger.errorLogEntry("error 7876");
                }
                s.close();
            }
            conn.commit();
        } catch (Throwable e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
    }

	/*
     * public boolean getDBVersion(){ try{
	 * DatabaseUtil.getDBVersion(connectionURL, sysDbVersion, appDbVersion); }
	 *
	 * return false; }
	 */

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

    public static void main(String[] args) throws SQLException {
        // DatabaseDeployer dd=new DatabaseDeployer("test");
        // dd.deploy();
        // dd.testInsert();
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

    public boolean checkAndCreateIndex(String tableName, String indexName) {
        Connection conn = dbPool.getConnection();
        try {
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet rs = metadata.getIndexInfo(null, "public", tableName, false, false);
            while (rs.next()) {
                if (rs.getString("INDEX_NAME").equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
            createTrigger("create index " + indexName + " on " + tableName + " using gin (fts)");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            dbPool.returnConnection(conn);
        }
        return true;
    }

    @Override
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
                        try {
                            script = handler.getScript();
                            ScriptProcessor sp = new ScriptProcessor();
                            IScriptSource myObject = sp.setScriptLauncher(script, false);
                            _Session session = new _Session(env, new User(Const.sysUser), this);
                            myObject.setSession(session);
                            myObject.setConnection(scriptConn);
                            String resObj = myObject.patchHandlerProcess();
                            executeQuery("insert into patches (PROCESSEDTIME, HASH, DESCRIPTION, NAME) VALUES ('"
                                    + new Timestamp(new Date().getTime()) + "', " + handler.hashCode() + ", " + "'"
                                    + handler.description + "', '" + handler.id + "')");
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

    public boolean checkAndCreateView(String scriptCreateView, String viewName) {
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            if (!DatabaseUtil.hasView(viewName, conn)) {
                try {
                    s.execute(scriptCreateView);
                    conn.commit();
                    s.close();
                    return true;
                } catch (SQLException sqle) {
                    AppEnv.logger.errorLogEntry("View " + viewName + " has not created");
                }
            }
            conn.commit();
            s.close();
        } catch (Throwable e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return false;
    }

    public boolean checkAndCreateMaterializedView(String scriptCreateView, String viewName) {
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            if (!DatabaseUtil.hasMaterializedView(viewName, conn)) {
                try {
                    s.execute(scriptCreateView);
                    conn.commit();
                    s.close();
                    return true;
                } catch (SQLException sqle) {
                    AppEnv.logger.errorLogEntry("View " + viewName + " has not created");
                }
            }
            conn.commit();
            s.close();
        } catch (Throwable e) {
            DatabaseUtil.debugErrorPrint(e);
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
