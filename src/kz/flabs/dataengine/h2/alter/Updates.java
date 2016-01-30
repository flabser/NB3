package kz.flabs.dataengine.h2.alter;

import kz.flabs.dataengine.DatabaseUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

public class Updates {
    public static String[] accessTables = {"READERS_MAINDOCS", "READERS_EXECUTIONS", "READERS_TASKS", "READERS_PROJECTS", "AUTHORS_MAINDOCS", "AUTHORS_EXECUTIONS", "AUTHORS_TASKS", "AUTHORS_PROJECTS"};
    public static String[] tableWithBlobs = {"MAINDOCS", "TASKS", "PROJECTS", "EXECUTIONS", "TOPICS", "POSTS", "EMPLOYERS", "GLOSSARY"};
    public static String[] basicDocTables = {"MAINDOCS", "TASKS", "PROJECTS", "EXECUTIONS", "TOPICS", "POSTS", "EMPLOYERS", "GLOSSARY", "DEPARTMENTS", "ORGANIZATIONS"};


    public static boolean runPatch(int version, Connection conn) throws Throwable {
        boolean result = false;
        Class c = Class.forName("kz.flabs.dataengine.h2.alter.Updates");
        String methodName = "updateToVersion" + Integer.toString(version);
        Class partypes[] = new Class[1];
        partypes[0] = Connection.class;
        Method m = c.getMethod(methodName, partypes);
        Object arglist[] = new Object[1];
        arglist[0] = conn;
        try {
            result = (Boolean) m.invoke(c, arglist);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;

    }


    boolean updateToVersion2(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        for (String table : accessTables) {
            statement.addBatch("UPDATE " + table + " SET USERNAME = '[observer]' where username='observer'");
        }
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        conn.commit();
        statement.close();
        return true;
    }

    boolean updateToVersion3(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE USER_ROLES ADD TYPE int NOT NULL");
        statement.addBatch("ALTER TABLE USER_GROUPS ADD EMPID int NOT NULL");
        statement.addBatch("ALTER TABLE USER_GROUPS ADD TYPE int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion4(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE TASKS ADD TASKVN varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion5(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE USERS_ACTIVITY ADD DBID varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion6(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table user_groups drop constraint constraint_ff9");
        statement.addBatch("alter table user_groups drop constraint constraint_c6c");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion7(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table glossary alter column viewtext type varchar_ignorecase(512)");
        statement.addBatch("alter table custom_fields_glossary alter column value type varchar_ignorecase(512)");
        statement.addBatch("alter table glossary alter column form type varchar_ignorecase(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion8(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table tasks add if not exists briefcontent varchar(512)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion9(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("update maindocs set parentdoctype = 890 where parentdoctype = 0");
        statement.addBatch("update tasks set parentdoctype = 890 where parentdoctype = 0");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion10(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("insert into counters (keys, lastnum) values ('task', 0)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion11(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table tasksexecutors add if not exists responsible varchar(512)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion12(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add if not exists owner int; " +
                " alter table groups add if not exists type int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion13(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups alter column owner type varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion14(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        s.addBatch("alter table users_activity_changes add column fieldtype int");
        try {
            s.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        s.close();
        return true;
    }

    boolean updateToVersion15(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        s.addBatch("create table USERS_ACTIVITY_BLOBCHANGES (id int generated by default as identity PRIMARY KEY, docid int, doctype int, name varchar(32), type int, originalname varchar(128), cheksum varchar(40), value blob)");
        try {
            s.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        s.close();
        return true;
    }

    boolean updateToVersion16(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        s.addBatch("alter table users_activity add column viewtext varchar(2048)");
        try {
            s.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        s.close();
        return true;
    }

    boolean updateToVersion17(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table users_activity_changes alter column type newvalue varchar(2048)");
        statement.addBatch("alter table users_activity_changes alter column type oldvalue varchar(2048)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion18(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table tasks add column har int");
        statement.addBatch("alter table tasks add column project int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion19(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table maindocs add column defaultruleid varchar(32)");
        statement.addBatch("alter table tasks add column defaultruleid varchar(32)");
        statement.addBatch("alter table executions add column defaultruleid varchar(32)");
        statement.addBatch("alter table projects add column defaultruleid varchar(32)");
        statement.addBatch("alter table glossary add column defaultruleid varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion20(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column har int");
        statement.addBatch("alter table projects add column project int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion21(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("insert into counters (keys, lastnum) values ('l', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('ord', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('contract', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('in', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('ish', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('task', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('sz', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('workdocprj', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('outdocprj', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('ordprj', 0)");
        statement.addBatch("insert into counters (keys, lastnum) values ('contractprj', 0)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion22(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE counters ADD CONSTRAINT COUNTERS_KEYS UNIQUE (keys);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion23(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table maindocs add column viewtext1 varchar(256);");
        statement.addBatch("alter table maindocs add column viewtext2 varchar(256);");
        statement.addBatch("alter table maindocs add column viewtext3 varchar(256);");
        statement.addBatch("alter table maindocs add column viewnumber int;");
        statement.addBatch("alter table maindocs add column viewdate timestamp;");

        statement.addBatch("alter table tasks add column viewtext1 varchar(256);");
        statement.addBatch("alter table tasks add column viewtext2 varchar(256);");
        statement.addBatch("alter table tasks add column viewtext3 varchar(256);");
        statement.addBatch("alter table tasks add column viewnumber int;");
        statement.addBatch("alter table tasks add column viewdate timestamp;");

        statement.addBatch("alter table executions add column viewtext1 varchar(256);");
        statement.addBatch("alter table executions add column viewtext2 varchar(256);");
        statement.addBatch("alter table executions add column viewtext3 varchar(256);");
        statement.addBatch("alter table executions add column viewnumber int;");
        statement.addBatch("alter table executions add column viewdate timestamp;");

        statement.addBatch("alter table glossary add column viewtext1 varchar(256);");
        statement.addBatch("alter table glossary add column viewtext2 varchar(256);");
        statement.addBatch("alter table glossary add column viewtext3 varchar(256);");
        statement.addBatch("alter table glossary add column viewnumber int;");
        statement.addBatch("alter table glossary add column viewdate timestamp;");

        statement.addBatch("alter table projects add column viewtext1 varchar(256);");
        statement.addBatch("alter table projects add column viewtext2 varchar(256);");
        statement.addBatch("alter table projects add column viewtext3 varchar(256);");
        statement.addBatch("alter table projects add column viewnumber int;");
        statement.addBatch("alter table projects add column viewdate timestamp;");

        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion24(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE tasksexecutors add column execpercent int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion25(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add column viewtext1 varchar(256);");
        statement.addBatch("alter table groups add column viewtext2 varchar(256);");
        statement.addBatch("alter table groups add column viewtext3 varchar(256);");
        statement.addBatch("alter table groups add column viewnumber int;");
        statement.addBatch("alter table groups add column viewdate timestamp;");

        statement.addBatch("alter table roles add column viewtext1 varchar(256);");
        statement.addBatch("alter table roles add column viewtext2 varchar(256);");
        statement.addBatch("alter table roles add column viewtext3 varchar(256);");
        statement.addBatch("alter table roles add column viewnumber int;");
        statement.addBatch("alter table roles add column viewdate timestamp;");

        statement.addBatch("alter table roles add column defaultruleid varchar(32)");
        statement.addBatch("alter table groups add column defaultruleid varchar(32)");

        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion26(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table maindocs add column SIGN varchar(1600);");
        statement.addBatch("alter table maindocs add column SIGNEDFIELDS varchar(1200);");

        statement.addBatch("alter table tasks add column SIGN varchar(1600);");
        statement.addBatch("alter table tasks add column SIGNEDFIELDS varchar(1200);");

        statement.addBatch("alter table executions add column SIGN varchar(1600);");
        statement.addBatch("alter table executions add column SIGNEDFIELDS varchar(1200);");

        statement.addBatch("alter table projects add column SIGN varchar(1600);");
        statement.addBatch("alter table projects add column SIGNEDFIELDS varchar(1200);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion27(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table glossary add column SIGN varchar(1600);");
        statement.addBatch("alter table glossary add column SIGNEDFIELDS varchar(1200);");

        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion28(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add column parentdocid int;");
        statement.addBatch("alter table groups add column parentdoctype int;");
        statement.addBatch("alter table projects add column parentdocid int;");
        statement.addBatch("alter table projects add column parentdoctype int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion29(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table roles alter column rolename type varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion30(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table coordblocks add column coordate timestamp");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion31(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table readers_maindocs add column favorites int default 0;");
        statement.addBatch("alter table readers_tasks add column favorites int default 0;");
        statement.addBatch("alter table readers_executions add column favorites int default 0;");
        statement.addBatch("alter table readers_projects add column favorites int default 0;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion32(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table filter drop constraint filter_userid_fkey;");
        statement.addBatch("alter table filter alter column userid type varchar(128);");
        statement.addBatch("alter table filter add constraint filter_userid_fkey foreign key (userid) references employers (userid);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion33(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE condition drop CONSTRAINT condition_fid_fkey;");
        statement.addBatch("ALTER TABLE condition ADD CONSTRAINT condition_fid_fkey FOREIGN KEY (fid) REFERENCES filter (id) ON DELETE CASCADE;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion34(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table tasks add if not exists  category int");
        statement.addBatch("alter table projects add if not exists  category int");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion35(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table condition add if not exists  name varchar(128)");
        statement.addBatch("alter table condition add if not exists  value varchar(256)");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion36(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("delete from condition;");
        statement.addBatch("alter table condition drop column formula;");
        statement.addBatch("alter table condition drop column enable;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion37(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column origin varchar(1024);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion38(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add if not exists  viewtext1 varchar(256);");
        statement.addBatch("alter table projects add if not exists  viewtext2 varchar(256);");
        statement.addBatch("alter table projects add if not exists  viewtext3 varchar(256);");
        statement.addBatch("alter table projects add if not exists  viewnumber int;");
        statement.addBatch("alter table projects add if not exists  viewdate timestamp;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion39(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add if not exists  coordinats varchar(256);");
        statement.addBatch("alter table projects add if not exists  city int;");
        statement.addBatch("alter table projects add if not exists  street varchar(256);");
        statement.addBatch("alter table projects add if not exists  house varchar(256);");
        statement.addBatch("alter table projects add if not exists  porch varchar(256);");
        statement.addBatch("alter table projects add if not exists  floor varchar(256);");
        statement.addBatch("alter table projects add if not exists  apartment varchar(256);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion40(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add if not exists responsible varchar(32);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion41(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add if not exists ctrldate timestamp;");
        statement.addBatch("alter table projects add if not exists subcategory int;");
        statement.addBatch("alter table projects add if not exists contragent varchar(256);");
        statement.addBatch("alter table projects add if not exists podryad varchar(256);");
        statement.addBatch("alter table projects add if not exists subpodryad varchar(256);");
        statement.addBatch("alter table projects add if not exists executor varchar(32);");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion42(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table projects alter column origin type varchar(1024);");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        conn.commit();
        return true;
    }

    boolean updateToVersion43(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table tasks add column DBD int DEFAULT 0;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;

    }

    boolean updateToVersion44(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table tasks add column CUSTOMER int;");
        statement.addBatch("alter table tasks add column APPS int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion45(Connection conn) throws Exception {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.addBatch("alter table maindocs add column topicid bigint;");
            statement.addBatch("alter table maindocs add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
            statement.addBatch("alter table tasks add column topicid bigint;");
            statement.addBatch("alter table tasks add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
            statement.addBatch("alter table executions add column topicid bigint;");
            statement.addBatch("alter table executions add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            statement.close();
            conn.commit();
        }
        return true;
    }

    public boolean updateToVersion46(Connection conn) throws Exception {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.addBatch("alter table projects add column topicid bigint;");
            statement.addBatch("alter table projects add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            statement.close();
            conn.commit();
        }
        return true;
    }

    public boolean updateToVersion47(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table projects add column DBD int DEFAULT 0;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;

    }

    public boolean updateToVersion48(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table topics add ISPUBLIC int DEFAULT 0;");
        statement.addBatch("alter table posts add ISPUBLIC int DEFAULT 0;");
        statement.addBatch("alter table employers add INDEXNUMBER varchar(32) DEFAULT 0;");
        statement.addBatch("alter table departments add INDEXNUMBER varchar(32) DEFAULT 0;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion49(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table projects add RESPOST varchar(256);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion50(Connection conn) throws Exception {
        Statement statement = null;
        for (String table : tableWithBlobs) {
            statement = conn.createStatement();
            statement.addBatch("alter table CUSTOM_BLOBS_" + table + " add COMMENT varchar(64);");
            try {
                statement.executeBatch();
            } catch (Exception e) {
                DatabaseUtil.debugErrorPrint(e);
            }
        }
        statement.close();
        conn.commit();
        return true;
    }


    public boolean updateToVersion51(Connection conn) throws Exception {
        try {
            Statement statement = null;
            statement = conn.createStatement();
            //statement.addBatch("drop table ROLES;");
            //statement.addBatch("alter table USER_ROLES drop constraint CONSTRAINT_C6C7");
            statement.addBatch("alter table USER_ROLES add column NAME varchar(32);");
            statement.addBatch("alter table USER_ROLES drop column ROLEID;");
            statement.executeBatch();
            statement.close();
            conn.commit();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        return true;
    }


    public boolean updateToVersion52(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table employers add OBL int;");
        statement.addBatch("alter table employers add REGION int;");
        statement.addBatch("alter table employers add VILLAGE int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion53(Connection conn) throws Exception {
        Statement statement = null;
        for (String table : basicDocTables) {
            statement = conn.createStatement();
            statement.addBatch("alter table " + table + " add DDBID varchar(16);");
            try {
                statement.executeBatch();
            } catch (Exception e) {
                DatabaseUtil.debugErrorPrint(e);
            }
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion54(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table projects add amountdamage varchar(1024);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion55(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();

        statement.addBatch("alter table CUSTOM_BLOBS_EMPLOYERS add COMMENT varchar(64);");

        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }


    public boolean updateToVersion56(Connection conn) throws Exception {
        Statement statement = null;

        for (String table : tableWithBlobs) {
            statement = conn.createStatement();
            statement.addBatch("update \"PUBLIC\".\"CUSTOM_BLOBS_" + table + "\" set \"COMMENT\"='' where \"COMMENT\" is NULL");
            try {
                statement.executeBatch();
                statement.close();
                conn.commit();
            } catch (Exception e) {
                DatabaseUtil.debugErrorPrint(e);
                statement.close();
                conn.rollback();
                //return false;
            }
        }
        return true;
    }

    public boolean updateToVersion57(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("ALTER TABLE employers alter column index rename to indexnumber;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion58(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("ALTER TABLE departments alter column index rename to indexnumber;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion59(Connection conn) throws Exception {
        Statement statement = null;
        for (String table : basicDocTables) {
            statement = conn.createStatement();
            statement.addBatch("alter table " + table + " add DDBID varchar(16);");
            try {
                statement.executeBatch();
            } catch (Exception e) {
                DatabaseUtil.debugErrorPrint(e);
            }
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion60(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table glossary add has_attachment int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion61(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table user_roles alter name varchar(64);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion62(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column parentdocid int");
        statement.addBatch("alter table projects add column parentdoctype int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion63(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column category int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion64(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column AMOUNTDAMAGE VARCHAR(1024)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion65(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table user_roles add column APP VARCHAR(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion66(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table employers add column BIRTHDATE timestamp");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion67(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add column PARENTDOCID int");
        statement.addBatch("alter table groups add column PARENTDOCTYPE int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion68(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table employers add column status int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion69(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table maindocs add column viewtext4 varchar(128)");
        statement.addBatch("alter table maindocs add column viewtext5 varchar(128)");
        statement.addBatch("alter table maindocs add column viewtext6 varchar(128)");
        statement.addBatch("alter table maindocs add column viewtext7 varchar(128)");

        statement.addBatch("alter table topics add column viewtext4 varchar(128)");
        statement.addBatch("alter table topics add column viewtext5 varchar(128)");
        statement.addBatch("alter table topics add column viewtext6 varchar(128)");
        statement.addBatch("alter table topics add column viewtext7 varchar(128)");

        statement.addBatch("alter table posts add column viewtext4 varchar(128)");
        statement.addBatch("alter table posts add column viewtext5 varchar(128)");
        statement.addBatch("alter table posts add column viewtext6 varchar(128)");
        statement.addBatch("alter table posts add column viewtext7 varchar(128)");

        statement.addBatch("alter table tasks add column viewtext4 varchar(128)");
        statement.addBatch("alter table tasks add column viewtext5 varchar(128)");
        statement.addBatch("alter table tasks add column viewtext6 varchar(128)");
        statement.addBatch("alter table tasks add column viewtext7 varchar(128)");

        statement.addBatch("alter table projects add column viewtext4 varchar(128)");
        statement.addBatch("alter table projects add column viewtext5 varchar(128)");
        statement.addBatch("alter table projects add column viewtext6 varchar(128)");
        statement.addBatch("alter table projects add column viewtext7 varchar(128)");


        statement.addBatch("alter table executions add column viewtext4 varchar(128)");
        statement.addBatch("alter table executions add column viewtext5 varchar(128)");
        statement.addBatch("alter table executions add column viewtext6 varchar(128)");
        statement.addBatch("alter table executions add column viewtext7 varchar(128)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion70(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table glossary add column viewtext4 varchar(128)");
        statement.addBatch("alter table glossary add column viewtext5 varchar(128)");
        statement.addBatch("alter table glossary add column viewtext6 varchar(128)");
        statement.addBatch("alter table glossary add column viewtext7 varchar(128)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion71(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add column viewtext4 varchar(128)");
        statement.addBatch("alter table groups add column viewtext5 varchar(128)");
        statement.addBatch("alter table groups add column viewtext6 varchar(128)");
        statement.addBatch("alter table groups add column viewtext7 varchar(128)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion72(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table glossary add if not exists sign varchar(1600)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion73(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table maindocs alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table topics alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table posts alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table tasks alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table executions alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table projects alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table glossary alter viewnumber type numeric(19,4)");
        statement.addBatch("alter table groups alter viewnumber type numeric(19,4)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion74(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table users_activity add ddbid varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion75(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table employers add viewtext1 varchar(256)");
        statement.addBatch("alter table employers add viewtext2 varchar(256)");
        statement.addBatch("alter table employers add viewtext3 varchar(256)");
        statement.addBatch("alter table employers add viewtext4 varchar(128)");
        statement.addBatch("alter table employers add viewtext5 varchar(128)");
        statement.addBatch("alter table employers add viewtext6 varchar(128)");
        statement.addBatch("alter table employers add viewtext7 varchar(128)");
        statement.addBatch("alter table employers add viewnumber numeric(19, 4)");
        statement.addBatch("alter table employers add viewdate timestamp");

        statement.addBatch("alter table departments add viewtext1 varchar(256)");
        statement.addBatch("alter table departments add viewtext2 varchar(256)");
        statement.addBatch("alter table departments add viewtext3 varchar(256)");
        statement.addBatch("alter table departments add viewtext4 varchar(128)");
        statement.addBatch("alter table departments add viewtext5 varchar(128)");
        statement.addBatch("alter table departments add viewtext6 varchar(128)");
        statement.addBatch("alter table departments add viewtext7 varchar(128)");
        statement.addBatch("alter table departments add viewnumber numeric(19, 4)");
        statement.addBatch("alter table departments add viewdate timestamp");

        statement.addBatch("alter table organizations add viewtext1 varchar(256)");
        statement.addBatch("alter table organizations add viewtext2 varchar(256)");
        statement.addBatch("alter table organizations add viewtext3 varchar(256)");
        statement.addBatch("alter table organizations add viewtext4 varchar(128)");
        statement.addBatch("alter table organizations add viewtext5 varchar(128)");
        statement.addBatch("alter table organizations add viewtext6 varchar(128)");
        statement.addBatch("alter table organizations add viewtext7 varchar(128)");
        statement.addBatch("alter table organizations add viewnumber numeric(19, 4)");
        statement.addBatch("alter table organizations add viewdate timestamp");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion76(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table glossary add SIGNEDFIELDS varchar(1200)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion77(Connection conn) throws Exception {
        return true;
    }

    public boolean updateToVersion78(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table topics add DDBID varchar(16)");
        statement.addBatch("alter table posts add DDBID varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion79(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table GLOSSARY add TOPICID bigint");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion80(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table topics add if not exists DDBID varchar(16)");
        statement.addBatch("alter table posts add if not exists DDBID varchar(16)");
        statement.addBatch("alter table GLOSSARY if not exists add TOPICID bigint");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion81(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add if not exists viewtext varchar(2048)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }


    public boolean updateToVersion82(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table custom_fields_glossary alter valueasnumber type numeric(19,4)");
        statement.addBatch("alter table custom_fields alter valueasnumber type numeric(19,4)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion83(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table maindocs alter sign type varchar(6144)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion84(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table MAINDOCS add PARENTDOCDDBID  varchar(16)");
        statement.addBatch("alter table GLOSSARY add PARENTDOCDDBID  varchar(16)");
        statement.addBatch("alter table MAINDOCS add APPID  varchar(16)");
        statement.addBatch("alter table GLOSSARY add APPID  varchar(16)");
        statement.addBatch("alter table USER_ROLES add APPID  varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion85(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table STRUCTURE_TREE_PATH alter ANCESTOR type varchar(16)");
        statement.addBatch("alter table STRUCTURE_TREE_PATH alter DESCENDANT type varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion86(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table CUSTOM_BLOBS_MAINDOCS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_TASKS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_PROJECTS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_EXECUTIONS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_TOPICS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_POSTS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_EMPLOYERS add VALUE_OID oid");
        statement.addBatch("alter table CUSTOM_BLOBS_GLOSSARY add VALUE_OID oid");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion87(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table CUSTOM_BLOBS_MAINDOCS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_TASKS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_PROJECTS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_EXECUTIONS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_TOPICS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_POSTS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_EMPLOYERS add REGDATE timestamp");
        statement.addBatch("alter table CUSTOM_BLOBS_GLOSSARY add REGDATE timestamp");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion88(Connection conn) throws Exception {
        return true;
    }

    public boolean updateToVersion89(Connection conn) throws Exception {
        return true;
    }

    public boolean updateToVersion90(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table ORGANIZATIONS add BIN varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion91(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table user_roles alter appid type varchar(256)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion92(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table GROUPS add if not exists viewtext varchar(2048)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion93(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE MAINDOCS ADD IF NOT EXISTS HAS_RESPONSE INT");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion94(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE CUSTOM_FIELDS ADD IF NOT EXISTS VALUEASOBJECT CLOB(500M)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion95(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE users_activity ADD IF NOT EXISTS CLIENTIP char(15)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion96(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE CUSTOM_FIELDS ADD IF NOT EXISTS VALUEASCLOB CLOB(500M)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion97(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE maindocs ALTER COLUMN has_response SET DEFAULT 0;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }


    public boolean updateToVersion98(Connection conn) throws Exception {
        return (dropColumnConstraint(conn, "COORDBLOCKS", "DOCID")
                && addColumnConstraint(conn, "COORDBLOCKS", "DOCID", "MAINDOCS", "DOCID", true));
    }

    public boolean updateToVersion99(Connection conn) throws Exception {
        return (dropColumnConstraint(conn, "COORDBLOCKS", "DOCID")
                && addColumnConstraint(conn, "COORDBLOCKS", "DOCID", "MAINDOCS", "DOCID", true));
    }

    public boolean updateToVersion100(Connection conn) throws Exception {
        return alterColumnAlterType(conn, "COORDINATORS", "COMMENT", "varchar(1024)");
    }

    public boolean updateToVersion101(Connection connection) throws Exception {
        return addNewColumn(connection, "CUSTOM_BLOBS_EMPLOYERS", "COMMENT", "TEXT");
    }

    public boolean updateToVersion102(Connection connection) throws Exception {
        return dropColumnConstraint(connection, "CUSTOM_BLOBS_EMPLOYERS", "DOCID");
    }

    public boolean updateToVersion103(Connection connection) throws Exception {
        return addNewColumn(connection, "MAINDOCS", "HAS_TOPIC", "BOOLEAN DEFAULT FALSE");
    }

    public boolean updateToVersion104(Connection connection) throws Exception {
        return dropColumnConstraint(connection, "MAINDOCS", "TOPICID");
    }

    public static boolean addNewColumn(Connection conn, String tableName, String columnName, String typeNameAndSize) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table " + tableName + " add IF NOT EXISTS " + columnName + " " + typeNameAndSize);
        statement.executeBatch();
        statement.close();
        return true;
    }

    public static boolean alterColumnAlterType(Connection conn, String tableName, String columnName, String typeNameAndSize) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table " + tableName + " alter " + columnName + " type " + typeNameAndSize);
        statement.addBatch("alter table STRUCTURE_TREE_PATH alter DESCENDANT type varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    private static boolean addColumnConstraint(Connection conn, String baseTableName, String baseColumnName, String foreignTableName, String foreignColumnName, boolean cascadeDeletion) throws SQLException {
        try {
            String sql = "alter table ? add foreign key (?) REFERENCES ?(?) ";
            if (cascadeDeletion) {
                sql += " ON DELETE CASCADE";
            }
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, baseTableName);
            pst.setString(2, baseColumnName);
            pst.setString(3, foreignTableName);
            pst.setString(4, foreignColumnName);
            pst.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
    }

    private static boolean dropColumnConstraint(Connection conn, String tableName, String columnName) throws SQLException {
        PreparedStatement pst = conn.prepareStatement("select distinct constraint_name from information_schema.constraints\n" +
                "where table_name = ? and column_list = ?");
        pst.setString(1, tableName);
        pst.setString(2, columnName);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            pst = conn.prepareStatement("alter table ? drop CONSTRAINT ?");
            pst.setString(1, tableName);
            pst.setString(2, rs.getString(1));
            pst.execute();
            conn.commit();
            return true;
        }
        return false;
    }
}
