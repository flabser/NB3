package kz.flabs.dataengine.mssql.alter;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.mssql.useractivity.UsersActivityDDEScripts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;


public class Updates extends kz.flabs.dataengine.h2.alter.Updates {

    public static boolean runPatch(int version, Connection conn) throws Throwable {
        boolean result = false;
        Class c = Class.forName("kz.flabs.dataengine.mssql.alter.Updates");
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


    public static boolean updateToVersion2(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("DROP TABLE RECYCLE_BIN;");
        statement.addBatch("DROP TABLE USERS_ACTIVITY_CHANGES;");
        statement.addBatch("DROP TABLE USERS_ACTIVITY;");

        statement.addBatch(UsersActivityDDEScripts.getUsersActivityDDE());
        statement.addBatch(UsersActivityDDEScripts.getUsersActivityChangesDDE());
        statement.addBatch(UsersActivityDDEScripts.getRecycleBinDDE());

        statement.executeBatch();
        conn.commit();
        statement.close();
        return true;
    }

    public static boolean updateToVersion3(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table projects add RESPOST nvarchar(256);");
        statement.executeBatch();
        conn.commit();
        statement.close();
        return true;
    }

    public boolean updateToVersion50(Connection conn) throws Exception {
        Statement statement = null;
        for (String table : tableWithBlobs) {
            statement = conn.createStatement();
            statement.addBatch("alter table CUSTOM_BLOBS_" + table + " add column COMMENT varchar(64);");
            statement.executeBatch();
        }
        statement.close();
        return true;
    }

    public boolean updateToVersion60(Connection conn) throws Exception {
        Statement statement = null;
        statement = conn.createStatement();
        statement.addBatch("alter table glossary add HAS_ATTACHMENT int;");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    @Override
    public boolean updateToVersion65(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table user_roles add APP VARCHAR(32)");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    @Override
    public boolean updateToVersion66(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table employers add BIRTHDATE datetime");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    @Override
    public boolean updateToVersion67(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table groups add PARENTDOCID int");
        statement.addBatch("alter table groups add PARENTDOCTYPE int");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
        return true;
    }

    @Override
    public boolean updateToVersion68(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table employers add status int");
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
        statement.addBatch("alter table maindocs add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table maindocs add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table maindocs add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table maindocs add  viewtext7 nvarchar(128)");

        statement.addBatch("alter table topics add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table topics add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table topics add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table topics add  viewtext7 nvarchar(128)");

        statement.addBatch("alter table posts add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table posts add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table posts add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table posts add  viewtext7 nvarchar(128)");

        statement.addBatch("alter table tasks add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table tasks add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table tasks add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table tasks add  viewtext7 nvarchar(128)");

        statement.addBatch("alter table projects add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table projects add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table projects add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table projects add  viewtext7 nvarchar(128)");


        statement.addBatch("alter table executions add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table executions add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table executions add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table executions add  viewtext7 nvarchar(128)");
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
        statement.addBatch("alter table glossary add  viewtext4 nvarchar(128)");
        statement.addBatch("alter table glossary add  viewtext5 nvarchar(128)");
        statement.addBatch("alter table glossary add  viewtext6 nvarchar(128)");
        statement.addBatch("alter table glossary add  viewtext7 nvarchar(128)");
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
        statement.addBatch("alter table groups add viewtext4 nvarchar(128)");
        statement.addBatch("alter table groups add viewtext5 nvarchar(128)");
        statement.addBatch("alter table groups add viewtext6 nvarchar(128)");
        statement.addBatch("alter table groups add viewtext7 nvarchar(128)");
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
        statement.addBatch("if COLUMNPROPERTY( OBJECT_ID('GLOSSARY'),'SIGN','ColumnId') is null " +
                "begin " +
                "  alter table GLOSSARY " +
                "  add SIGN varchar(1600) " +
                "end");
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
        statement.addBatch("alter table maindocs alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table topics alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table posts alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table tasks alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table executions alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table projects alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table glossary alter column viewnumber numeric(19,4)");
        statement.addBatch("alter table groups alter column viewnumber numeric(19,4)");
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
        statement.addBatch("alter table users_activity add ddbid nvarchar(16)");
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
        statement.addBatch("alter table employers add VIEWTEXT1 nvarchar(256)");
        statement.addBatch("alter table employers add VIEWTEXT2 nvarchar(256)");
        statement.addBatch("alter table employers add VIEWTEXT3 nvarchar(256)");
        statement.addBatch("alter table employers add VIEWTEXT4 nvarchar(128)");
        statement.addBatch("alter table employers add VIEWTEXT5 nvarchar(128)");
        statement.addBatch("alter table employers add VIEWTEXT6 nvarchar(128)");
        statement.addBatch("alter table employers add VIEWTEXT7 nvarchar(128)");
        statement.addBatch("alter table employers add VIEWNUMBER numeric(19, 4)");
        statement.addBatch("alter table employers add VIEWDATE datetime");

        statement.addBatch("alter table departments add VIEWTEXT1 nvarchar(256)");
        statement.addBatch("alter table departments add VIEWTEXT2 nvarchar(256)");
        statement.addBatch("alter table departments add VIEWTEXT3 nvarchar(256)");
        statement.addBatch("alter table departments add VIEWTEXT4 nvarchar(128)");
        statement.addBatch("alter table departments add VIEWTEXT5 nvarchar(128)");
        statement.addBatch("alter table departments add VIEWTEXT6 nvarchar(128)");
        statement.addBatch("alter table departments add VIEWTEXT7 nvarchar(128)");
        statement.addBatch("alter table departments add VIEWNUMBER numeric(19, 4)");
        statement.addBatch("alter table departments add VIEWDATE datetime");

        statement.addBatch("alter table organizations add VIEWTEXT1 nvarchar(256)");
        statement.addBatch("alter table organizations add VIEWTEXT2 nvarchar(256)");
        statement.addBatch("alter table organizations add VIEWTEXT3 nvarchar(256)");
        statement.addBatch("alter table organizations add VIEWTEXT4 nvarchar(128)");
        statement.addBatch("alter table organizations add VIEWTEXT5 nvarchar(128)");
        statement.addBatch("alter table organizations add VIEWTEXT6 nvarchar(128)");
        statement.addBatch("alter table organizations add VIEWTEXT7 nvarchar(128)");
        statement.addBatch("alter table organizations add VIEWNUMBER numeric(19, 4)");
        statement.addBatch("alter table organizations add VIEWDATE datetime");
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
        statement.addBatch("alter table glossary add SIGNEDFIELDS nvarchar(1200)");
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
        statement.addBatch("alter table GLOSSARY add TOPICID int");
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
        statement.addBatch("IF COL_LENGTH('topics', 'DDBID') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE topics\n" +
                "        ADD [DDBID] varchar(16)\n" +
                "END");
        statement.addBatch("IF COL_LENGTH('posts', 'DDBID') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE posts\n" +
                "        ADD [DDBID] varchar(16)\n" +
                "END");
        statement.addBatch("IF COL_LENGTH('GLOSSARY', 'TOPICID') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE GLOSSARY\n" +
                "        ADD [TOPICID] bigint\n" +
                "END");
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
        statement.addBatch("IF COL_LENGTH('groups', 'viewtext') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE groups\n" +
                "        ADD [viewtext] varchar(2048)\n" +
                "END");
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
        statement.addBatch("alter table custom_fields_glossary alter column valueasnumber numeric(19,4)");
        statement.addBatch("alter table custom_fields alter column valueasnumber numeric(19,4)");
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
        statement.addBatch("alter table maindocs alter column sign varchar(6144)");
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
        statement.addBatch("alter table MAINDOCS add  PARENTDOCDDBID  nvarchar(16)");
        statement.addBatch("alter table GLOSSARY add  PARENTDOCDDBID  nvarchar(16)");
        statement.addBatch("alter table MAINDOCS add  APPID  nvarchar(16)");
        statement.addBatch("alter table GLOSSARY add  APPID  nvarchar(16)");
        statement.addBatch("alter table USER_ROLES add  APPID  nvarchar(16)");
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
        statement.addBatch("alter table STRUCTURE_TREE_PATH alter column ANCESTOR varchar(16)");
        statement.addBatch("alter table STRUCTURE_TREE_PATH alter column DESCENDANT varchar(16)");
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
    /*    Statement statement = conn.createStatement();
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
        conn.commit();*/
        return true;
    }

    public boolean updateToVersion87(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("alter table CUSTOM_BLOBS_MAINDOCS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_TASKS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_PROJECTS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_EXECUTIONS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_TOPICS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_POSTS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_EMPLOYERS add REGDATE datetime");
        statement.addBatch("alter table CUSTOM_BLOBS_GLOSSARY add REGDATE datetime");
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
        statement.addBatch("alter table user_roles alter column appid varchar(256)");
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
        statement.addBatch("IF COL_LENGTH('groups', 'viewtext') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE groups\n" +
                "        ADD [viewtext] varchar(2048)\n" +
                "END");
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

    public boolean updateToVersion93(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("IF COL_LENGTH('maindocs', 'has_response') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE maindocs\n" +
                "        ADD [has_response] int\n" +
                "END");
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

    public boolean updateToVersion94(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("IF COL_LENGTH('custom_fields', 'valueasobject') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE custom_fields\n" +
                "        ADD [valueasobject] xml\n" +
                "END");
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

    public boolean updateToVersion95(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("IF COL_LENGTH('users_activity', 'clientip') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE users_activity\n" +
                "        ADD [clientip] char(15)\n" +
                "END");
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

    public boolean updateToVersion96(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("IF COL_LENGTH('custom_fields', 'valueasclob') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE custom_fields\n" +
                "        ADD [valueasclob] text\n" +
                "END");
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

    public boolean updateToVersion97(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        try {
            statement.addBatch("ALTER TABLE dbo.MAINDOCS DROP CONSTRAINT has_resp_def_constraint;\n");
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        try {
            statement.addBatch("ALTER TABLE dbo.MAINDOCS ADD CONSTRAINT has_resp_def_constraint DEFAULT 0 FOR has_response;");
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
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
        return addColumn(connection, "CUSTOM_BLOBS_EMPLOYERS", "COMMENT", "TEXT");
    }

    public boolean updateToVeriosn102(Connection connection) throws Exception {
        return dropForeignKey(connection, "CUSTOM_BLOBS_EMPLOYERS", "DOCID");
    }

    public boolean updateToVersion103(Connection connection) throws Exception {
        return addColumn(connection, "MAINDOCS", "HAS_TOPIC", "BOOLEAN DEFAULT FALSE");
    }

    public boolean updateToVersion104(Connection connection) throws Exception {
        return dropForeignKey(connection, "MAINDOCS", "TOPICID");
    }

    public static boolean addColumn(Connection conn, String tableName, String columnName, String typeNameAndSize) throws Exception {
        Statement statement = conn.createStatement();
        statement.addBatch("IF COL_LENGTH('" + tableName + "', '" + columnName + "') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE " + tableName + "\n" +
                "        ADD [" + columnName + "] " + typeNameAndSize + "\n" +
                "END");
        try {
            statement.executeBatch();
        } catch (Exception e) {
            DatabaseUtil.debugErrorPrint(e);
        }
        statement.close();
        conn.commit();
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
/*("IF COL_LENGTH('topics', 'DDBID') IS NULL\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "        ALTER TABLE topics\n" +
                "        ADD [DDBID] varchar(16)\n" +
                "END");*/
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
            PreparedStatement pst = conn.prepareStatement("SELECT *" +
                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE\n" +
                    "WHERE TABLE_NAME =  ?\n" +
                    "AND COLUMN_NAME =  ?");
            pst.setString(1, tableName.toLowerCase());
            pst.setString(2, columnName.toLowerCase());
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getString("COLUMN_NAME").equalsIgnoreCase(columnName.toUpperCase())) {
                Statement st = conn.createStatement();
                st.execute("alter  table " + tableName + " drop CONSTRAINT " + rs.getString("CONSTRAINT_NAME"));
                conn.commit();
                return true;
            }
        }
        return false;
    }
}
