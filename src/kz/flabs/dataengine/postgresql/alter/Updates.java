package kz.flabs.dataengine.postgresql.alter;

import kz.flabs.dataengine.DatabaseUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

public class Updates extends kz.flabs.dataengine.h2.alter.Updates {


    public static boolean runPatch(int version, Connection conn) throws Throwable {
        boolean result = false;
        Class c = Class.forName("kz.flabs.dataengine.postgresql.alter.Updates");
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

    static boolean updateToVersion2(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        for (String table : accessTables) {
            statement.addBatch("UPDATE " + table + " SET USERNAME = '[observer]' where username='observer'");
        }
        statement.executeBatch();
        conn.commit();
        statement.close();
        return true;
    }

    static boolean updateToVersion3(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add owner int;");
        statement.addBatch("alter table groups add type int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            return false;
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion4(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE custom_blobs_employers ADD PRIMARY KEY (id);");
        statement.addBatch("ALTER TABLE custom_blobs_executions ADD PRIMARY KEY (id);");
        statement.addBatch("ALTER TABLE custom_blobs_maindocs ADD PRIMARY KEY (id);");
        statement.addBatch("ALTER TABLE custom_blobs_projects ADD PRIMARY KEY (id);");
        statement.addBatch("ALTER TABLE custom_blobs_tasks ADD PRIMARY KEY (id);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
            return false;
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion5(Connection conn) throws SQLException {
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

    static boolean updateToVersion6(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        s.addBatch("create table USERS_ACTIVITY_BLOBCHANGES (id serial primary key, docid int, doctype int, name varchar(32), type int, originalname varchar(128), cheksum varchar(40), value bytea)");
        try {
            s.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        s.close();
        return true;
    }

    static boolean updateToVersion7(Connection conn) throws SQLException {
        Statement s = conn.createStatement();
        s.addBatch("alter table tasksexecutors drop constraint tasksexecutors_pkey");
        s.addBatch("alter table tasksexecutors add primary key(id)");
        try {
            s.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        s.close();
        return true;
    }

    static boolean updateToVersion8(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups alter column owner type varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion9(Connection conn) throws SQLException {
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

    static boolean updateToVersion10(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table users_activity_changes alter column newvalue type varchar(2048)");
        statement.addBatch("alter table users_activity_changes alter column oldvalue type varchar(2048)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion11(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table tasks add column har int");
        statement.addBatch("alter table tasks add column project int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion12(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion13(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column har int");
        statement.addBatch("alter table projects add column project int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion14(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion15(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE counters ADD CONSTRAINT COUNTERS_KEYS UNIQUE (keys);");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion16(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion17(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE tasksexecutors add column execpercent int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion18(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion19(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion20(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table glossary add column SIGN varchar(1600);");
        statement.addBatch("alter table glossary add column SIGNEDFIELDS varchar(1200);");

        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion21(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion22(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        //-- add fts tsvector column
        statement.addBatch("alter table maindocs add column fts tsvector;");
        statement.addBatch("alter table custom_fields add column fts tsvector;");
        statement.addBatch("alter table tasks add column fts tsvector;");
        statement.addBatch("alter table executions add column fts tsvector;");
        statement.addBatch("alter table projects add column fts tsvector;");

        //-- create index fts
        statement.addBatch("create index maindocs_fts_idx on maindocs using gin (fts);");
        statement.addBatch("create index custom_fields_fts_idx on custom_fields using gin (fts);");
        statement.addBatch("create index tasks_fts_idx on tasks using gin (fts);");
        statement.addBatch("create index executions_fts_idx on executions using gin (fts);");
        statement.addBatch("create index projects_fts_idx on projects using gin (fts);");

        try {
            System.out.println("add fts tsvector column / create index fts");
            statement.executeBatch();
            System.out.println("done");
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion23(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        //-- update fts tsvector
        statement.addBatch("UPDATE maindocs SET fts= " +
                " setweight( coalesce( to_tsvector(viewtext),''),'A')||" +
                " setweight( coalesce( to_tsvector(viewnumber::text),''),'B')||" +
                " setweight( coalesce( to_tsvector(viewdate::text),''),'C');");

        statement.addBatch("UPDATE custom_fields SET fts= " +
                " setweight( coalesce( to_tsvector(value),''),'A')||" +
                " setweight( coalesce( to_tsvector(valueasnumber::text),''),'B')||" +
                " setweight( coalesce( to_tsvector(valueasdate::text),''),'C');");

        statement.addBatch("UPDATE tasks SET fts= " +
                " setweight( coalesce( to_tsvector(viewtext),''),'A')||" +
                " setweight( coalesce( to_tsvector(viewnumber::text),''),'B')||" +
                " setweight( coalesce( to_tsvector(viewdate::text),''),'C');");

        statement.addBatch("UPDATE executions SET fts= " +
                " setweight( coalesce( to_tsvector(viewtext),''),'A')||" +
                " setweight( coalesce( to_tsvector(viewnumber::text),''),'B')||" +
                " setweight( coalesce( to_tsvector(viewdate::text),''),'C');");

        statement.addBatch("UPDATE projects SET fts= " +
                " setweight( coalesce( to_tsvector(viewtext),''),'A')||" +
                " setweight( coalesce( to_tsvector(viewnumber::text),''),'B')||" +
                " setweight( coalesce( to_tsvector(viewdate::text),''),'C');");

        try {
            System.out.println("update fts tsvector... please wait.\na very long process, when a large amount of data in the database...");
            statement.executeBatch();
            System.out.println("update fts tsvector done.");
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion24(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table roles alter column rolename type varchar(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion25(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table coordblocks add column coordate timestamp");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion26(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion27(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion28(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE condition ADD CONSTRAINT condition_fid_fkey FOREIGN KEY (fid) REFERENCES filter (id) ON DELETE CASCADE;");
        statement.addBatch(" ALTER TABLE condition drop CONSTRAINT condition_fid_fkey;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion29(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table tasks add column category int");
        statement.addBatch("alter table projects add column category int");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion30(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table condition add column name varchar(128)");
        statement.addBatch("alter table condition add column value varchar(256)");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion31(Connection conn) throws SQLException {
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
        return true;
    }

    static boolean updateToVersion32(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add column origin varchar(1024);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion33(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add coordinats varchar(256);");
        statement.addBatch("alter table projects add city int;");
        statement.addBatch("alter table projects add street varchar(256);");
        statement.addBatch("alter table projects add house varchar(256);");
        statement.addBatch("alter table projects add porch varchar(256);");
        statement.addBatch("alter table projects add floor varchar(256);");
        statement.addBatch("alter table projects add apartment varchar(256);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion34(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add responsible varchar(32);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion35(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add ctrldate timestamp;");
        statement.addBatch("alter table projects add subcategory int;");
        statement.addBatch("alter table projects add contragent varchar(256);");
        statement.addBatch("alter table projects add podryad varchar(256);");
        statement.addBatch("alter table projects add subpodryad varchar(256);");
        statement.addBatch("alter table projects add executor varchar(32);");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion36(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table projects alter column origin type varchar(1024);");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion37(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table tasks add column DBD int DEFAULT 0;");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion38(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table tasks add column APPS int;");
        statement.addBatch("alter table tasks add column CUSTOMER int;");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    public static boolean updateToVersion39(Connection conn) throws SQLException {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table maindocs add column topicid bigint;");
        statement.addBatch("alter table maindocs add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
        statement.addBatch("alter table tasks add column topicid bigint;");
        statement.addBatch("alter table tasks add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
        statement.addBatch("alter table executions add column topicid bigint;");
        statement.addBatch("alter table executions add constraint Topics_TopicID_FK foreign key (topicid) references Topics(docid) on update cascade on delete cascade;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
            return false;
        }
        statement.close();
        return true;
    }

    public static boolean updateToVersion40(Connection conn) throws SQLException {
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
        }
        return true;
    }

    static boolean updateToVersion41(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table projects add column DBD int DEFAULT 0;");

        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion42(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table topics add column ispublic int DEFAULT 0;");
        statement.addBatch("alter table posts add column ispublic int DEFAULT 0;");
        statement.addBatch("alter table employers add column INDEXNUMBER varchar(32) DEFAULT 0;");
        statement.addBatch("alter table departments add column INDEXNUMBER varchar(32) DEFAULT 0;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    static boolean updateToVersion43(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.addBatch("alter table projects add column respost varchar(256);");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    public boolean updateToVersion51(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("drop table ROLES;");
        statement.addBatch("alter table USER_ROLES add column NAME varchar(32);");
        statement.addBatch("alter table USER_ROLES drop column ROLEID;");
        statement.executeBatch();
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion57(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE employers RENAME COLUMN INDEX to INDEXNUMBER;");
        try {
            statement.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            statement.close();
        }
        statement.close();
        return true;
    }

    public boolean updateToVersion58(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("ALTER TABLE departments rename column index to indexnumber;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion60(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
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
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table user_roles alter name type varchar(64);");
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
        statement.addBatch("alter table projects add  parentdocid int");
        statement.addBatch("alter table projects add  parentdoctype int");
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
        statement.addBatch("alter table projects add  category int");
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
        statement.addBatch("alter table projects add  AMOUNTDAMAGE VARCHAR(1024)");
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
        statement.addBatch("alter table user_roles add  APP VARCHAR(32)");
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
        statement.addBatch("alter table employers add  BIRTHDATE timestamp");
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
        statement.addBatch("alter table groups add  PARENTDOCID int");
        statement.addBatch("alter table groups add  PARENTDOCTYPE int");
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
        statement.addBatch("alter table employers add  status int");
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
        statement.addBatch("alter table maindocs add  viewtext4 varchar(128)");
        statement.addBatch("alter table maindocs add  viewtext5 varchar(128)");
        statement.addBatch("alter table maindocs add  viewtext6 varchar(128)");
        statement.addBatch("alter table maindocs add  viewtext7 varchar(128)");

        statement.addBatch("alter table topics add  viewtext4 varchar(128)");
        statement.addBatch("alter table topics add  viewtext5 varchar(128)");
        statement.addBatch("alter table topics add  viewtext6 varchar(128)");
        statement.addBatch("alter table topics add  viewtext7 varchar(128)");

        statement.addBatch("alter table posts add  viewtext4 varchar(128)");
        statement.addBatch("alter table posts add  viewtext5 varchar(128)");
        statement.addBatch("alter table posts add  viewtext6 varchar(128)");
        statement.addBatch("alter table posts add  viewtext7 varchar(128)");

        statement.addBatch("alter table tasks add  viewtext4 varchar(128)");
        statement.addBatch("alter table tasks add  viewtext5 varchar(128)");
        statement.addBatch("alter table tasks add  viewtext6 varchar(128)");
        statement.addBatch("alter table tasks add  viewtext7 varchar(128)");

        statement.addBatch("alter table projects add  viewtext4 varchar(128)");
        statement.addBatch("alter table projects add  viewtext5 varchar(128)");
        statement.addBatch("alter table projects add  viewtext6 varchar(128)");
        statement.addBatch("alter table projects add  viewtext7 varchar(128)");


        statement.addBatch("alter table executions add  viewtext4 varchar(128)");
        statement.addBatch("alter table executions add  viewtext5 varchar(128)");
        statement.addBatch("alter table executions add  viewtext6 varchar(128)");
        statement.addBatch("alter table executions add  viewtext7 varchar(128)");
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
        statement.addBatch("alter table glossary add  viewtext4 varchar(128)");
        statement.addBatch("alter table glossary add  viewtext5 varchar(128)");
        statement.addBatch("alter table glossary add  viewtext6 varchar(128)");
        statement.addBatch("alter table glossary add  viewtext7 varchar(128)");
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
        statement.addBatch("alter table groups add  viewtext4 varchar(128)");
        statement.addBatch("alter table groups add  viewtext5 varchar(128)");
        statement.addBatch("alter table groups add  viewtext6 varchar(128)");
        statement.addBatch("alter table groups add  viewtext7 varchar(128)");
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
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE glossary ADD COLUMN sign varchar(1600);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column sign already exists in glossary.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE glossary ADD COLUMN SIGNEDFIELDS varchar(1200);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column signfields already exists in glossary.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        Statement statement = null;
        for (String table : tableWithBlobs) {
            statement = conn.createStatement();
            statement.addBatch("alter table CUSTOM_BLOBS_" + table + " add VALUE_OID oid;");
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
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE topics ADD COLUMN DDBID varchar(16);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column DDBID already exists in topics.';" +
                "        END;" +
                "    END;" +
                "$$");
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE posts ADD COLUMN DDBID varchar(16);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column DDBID already exists in posts.';" +
                "        END;" +
                "    END;" +
                "$$");
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE GLOSSARY ADD COLUMN TOPICID bigint;" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column TOPICID already exists in glossary.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE groups ADD COLUMN viewtext varchar(2048);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column DDBID already exists in groups.';" +
                "        END;" +
                "    END;" +
                "$$");
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

        Statement statement = conn.createStatement();
        statement.addBatch("DROP FUNCTION set_attachment_flag() CASCADE");
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

    public boolean updateToVersion89(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("ALTER TABLE custom_blobs_executions DROP CONSTRAINT custom_blobs_executions_docid_fkey;");
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
            return false;
        }
        statement.close();
        conn.commit();
        return true;
    }

    public boolean updateToVersion92(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE groups ADD COLUMN viewtext varchar(2048);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column viewtext already exists in groups.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE maindocs ADD COLUMN has_response int;" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column has_response already exists in maindocs.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE CUSTOM_FIELDS ADD COLUMN VALUEASOBJECT xml;" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column valueasobject already exists in custom_fields.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE users_activity ADD COLUMN CLIENTIP char(15);" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column CLIENTIP already exists in users_activity.';" +
                "        END;" +
                "    END;" +
                "$$");
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
        return addColumn(conn, "custom_fields", "valueasclob", "text");
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

    private static boolean addColumn(Connection conn, String tableName, String columnName, String columnTypeAndSize) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("DO $$ " +
                "    BEGIN" +
                "        BEGIN" +
                "            ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnTypeAndSize + ";" +
                "        EXCEPTION" +
                "            WHEN duplicate_column THEN RAISE NOTICE 'column " + columnName + " already exists in " + tableName + "';" +
                "        END;" +
                "    END;" +
                "$$");
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
        dropForeignKey(conn, "COORDBLOCKS", "DOCID");
        return addForeignKey(conn, "COORDBLOCKS", "DOCID", "MAINDOCS", "DOCID", true);
    }

    public boolean updateToVersion99(Connection conn) throws Exception {
        dropForeignKey(conn, "COORDBLOCKS", "DOCID");
        return addForeignKey(conn, "COORDBLOCKS", "DOCID", "MAINDOCS", "DOCID", true);
    }

    public boolean updateToVersion100(Connection conn) throws Exception {
        return alterColumnAlterType(conn, "COORDINATORS", "COMMENT", "varchar(1024)");
    }

    public boolean updateToVersion101(Connection connection) throws Exception {
        return addColumn(connection, "CUSTOM_BLOBS_EMPLOYERS", "COMMENT", "TEXT") &&
                addColumn(connection, "CUSTOM_BLOBS_EMPLOYERS", "VALUE_OID", "OID");
    }

    public boolean updateToVersion102(Connection connection) throws Exception {
        return dropForeignKey(connection, "CUSTOM_BLOBS_EMPLOYERS", "DOCID");
    }

    public boolean updateToVersion103(Connection connection) throws Exception {
        return addColumn(connection, "MAINDOCS", "HAS_TOPIC", "BOOLEAN DEFAULT FALSE");
    }

    public boolean updateToVersion104(Connection connection) throws Exception {
        return dropForeignKey(connection, "MAINDOCS", "TOPICID");
    }

    public static boolean alterColumnAlterType(Connection conn, String tableName, String columnName, String typeNameAndSize) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table " + tableName + " alter " + columnName + " type " + typeNameAndSize);
       // statement.addBatch("alter table STRUCTURE_TREE_PATH alter DESCENDANT type varchar(16)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    private static boolean addForeignKey(Connection conn, String baseTableName, String baseColumnName, String foreignTableName, String foreignColumnName, boolean cascadeDeletion) throws SQLException {
        try {
            Statement st = conn.createStatement();
            String sql = "alter table " + baseTableName + " add foreign key (" + baseColumnName + ") REFERENCES " + foreignTableName + "(" + foreignColumnName + ") ";
            if (cascadeDeletion) {
                sql += " ON DELETE CASCADE";
            }
            st.execute(sql);
            conn.commit();
            return true;
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
            conn.rollback();
            return false;
        }
    }

    private static boolean dropForeignKey(Connection conn, String tableName, String columnName) throws SQLException {

        if (tableName != null && columnName != null) {
            try {
                PreparedStatement pst = conn.prepareStatement("SELECT\n" +
                        "    tc.constraint_name, tc.table_name, kcu.column_name, \n" +
                        "    ccu.table_name AS foreign_table_name,\n" +
                        "    ccu.column_name AS foreign_column_name \n" +
                        "FROM \n" +
                        "    information_schema.table_constraints AS tc \n" +
                        "    JOIN information_schema.key_column_usage AS kcu\n" +
                        "      ON tc.constraint_name = kcu.constraint_name\n" +
                        "    JOIN information_schema.constraint_column_usage AS ccu\n" +
                        "      ON ccu.constraint_name = tc.constraint_name\n" +
                        "WHERE constraint_type = 'FOREIGN KEY' AND tc.table_name=?;");
                pst.setString(1, tableName.toLowerCase());
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getString("column_name").equalsIgnoreCase(columnName.toLowerCase())) {
                    Statement st = conn.createStatement();
                    st.execute("alter table " + tableName + " drop CONSTRAINT " + rs.getString(1));
                    conn.commit();
                }
                return true;
            } catch (SQLException e) {
                DatabaseUtil.debugErrorPrint(e);
                conn.rollback();
                throw e;
            }
        }
        return false;
    }

}
