package kz.flabs.dataengine.h2.usersactivity;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.runtimeobj.viewentry.ViewEntryType;
import kz.flabs.servlets.BrowserType;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class UsersActivity implements IUsersActivity {
    protected IDatabase db;
    protected IDBConnectionPool dbPool;

    public UsersActivity() {

    }

    public UsersActivity(IDatabase db) {
        this.db = db;
        dbPool = db.getConnectionPool();
    }

    @Override
    public IDatabase getParentDatabase() {
        return db;
    }

    @Override
    public int isRead(Connection conn, int docID, int docType, String userName) {
        int result = 0;
        try {
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT ID FROM USERS_ACTIVITY WHERE TYPE=" + UsersActivityType.MARKED_AS_READ.getCode() + " AND DOCID=" + docID + " AND DOCTYPE=" + docType + " AND USERID='" + userName + "'";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                result = 1;
            }
            s.close();
            rs.close();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        }
        return result;
    }

    @Override
    public int isRead(int docID, int docType, String userName) {
        int result = 0;
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT ID FROM USERS_ACTIVITY WHERE TYPE=" + UsersActivityType.MARKED_AS_READ.getCode() + " AND DOCID=" + docID + " AND DOCTYPE=" + docType + " AND USERID='" + userName + "'";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                result = 1;
            }
            s.close();
            rs.close();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return result;
    }

    @Override
    public int postAddToFavorites(int docID, int docType, String userID, HashSet<String> groups) {
        int key = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            /*String sql = "INSERT INTO USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE) values ("
                    + UsersActivityType.MARKED_AS_FAVORITE.getCode() + ", '" + db.getDbID() + "','" + userID + "','" + Database.sqlDateTimeFormat.format(new java.util.Date())
                    + "', " + docID + ", " + docType + ")";*/
            /*String sql = "UPDATE " + DatabaseUtil.getReadersTableName(docType) + " set favorites = 1 where docid = " + docID + " and username = '" + userID + "' ";*/
            String sql = "UPDATE " + DatabaseUtil.getReadersTableName(docType) + " set favorites = 1 where docid = " + docID + " and username in (" + DatabaseUtil.prepareListToQuery(groups) + ") ";
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            conn.commit();
            s.close();
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
    public int postRemoveFromFavorites(int docID, int docType, String userID, HashSet<String> groups) {
        int key = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            /*String sql = "INSERT INTO USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE) values ("
                    + UsersActivityType.UNMARKED_AS_FAVORITE.getCode() + ", '" + db.getDbID() + "','" + userID + "','" + Database.sqlDateTimeFormat.format(new java.util.Date())
                    + "', " + docID + ", " + docType + ")";*/
            /*String sql = "UPDATE " + DatabaseUtil.getReadersTableName(docType) + " set favorites = 0 where docid = " + docID + " and username = '" + userID + "' ";*/
            String sql = "UPDATE " + DatabaseUtil.getReadersTableName(docType) + " set favorites = 0 where docid = " + docID + " and username in (" + DatabaseUtil.prepareListToQuery(groups) + ") ";
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            conn.commit();
            s.close();
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
    public int postMarkRead(int docID, int docType, User user) {
        int key = 0;
            Connection conn = dbPool.getConnection();
            try {
                conn.setAutoCommit(false);
                Statement s = conn.createStatement();
                String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, CLIENTIP) values ("
                        + UsersActivityType.MARKED_AS_READ.getCode() + ",'" + db.getDbID() + "','" + user.getUserID() + "','" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
                        "', " + docID + ", " + docType + ", '" + user.getSession().ipAddr + "')";

                s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = s.getGeneratedKeys();
                if (rs.next()) {
                    key = rs.getInt(1);
                }
                conn.commit();
                s.close();
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
    public int postMarkUnread(int docID, int docType, User user) {
        int key = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, CLIENTIP) values ("
                    + UsersActivityType.MARKED_AS_UNREAD.getCode() + ",'" + db.getDbID() + "','" + user.getUserID() + "','" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
                    "', " + docID + ", " + docType + ", '" + user.getSession().ipAddr + "')";

            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            conn.commit();
            s.close();
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

    public int postLogin(BrowserType agent, User user) {
        int key = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, CLIENTIP) values ("
                    + UsersActivityType.LOGGED_IN.getCode() + ",'" + db.getDbID() + "', '" + user.getUserID()+ "', '" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
                    "', 0, 890, '" + user.getSession().ipAddr + "')";
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            conn.commit();
            s.close();
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

    public int postLogout(User user) {
        int key = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql = "insert into USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, CLIENTIP) values ("
                    + UsersActivityType.LOGGED_OUT.getCode() + ",'" + db.getDbID() + "', '" + user.getUserID() + "', '" + Database.sqlDateTimeFormat.format(new java.util.Date()) +
                    "', 0, 890, '" + user.getSession().ipAddr + "')";
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            conn.commit();
            s.close();
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
    public int postCompose(Document doc, User user) {
        PostCompose activityThread = new PostCompose(db, doc, user);
        activityThread.setPriority(Thread.MIN_PRIORITY);
        activityThread.start();
        return 1;
    }

    @Override
    public int postModify(Document oldDoc, Document modifiedDoc, User user) {
        PostModify activityThread = new PostModify(db, oldDoc, modifiedDoc, user);
        activityThread.setPriority(Thread.MIN_PRIORITY);
        activityThread.start();
        return 1;
    }

    @Override
    public int postDelete(BaseDocument doc, User user) {
        PostDelete activityThread = new PostDelete(db, doc, user);
        activityThread.setPriority(Thread.MIN_PRIORITY);
        activityThread.start();
        return 1;
    }

    @Override
    public int postCompletelyDelete(BaseDocument doc, User user) {
        PostCompletelyDelete activityThread = new PostCompletelyDelete(db, doc, user);
        activityThread.setPriority(Thread.MIN_PRIORITY);
        activityThread.start();
        return 1;
    }

    @Override
    public int postUndelete(BaseDocument doc, User user) {
        PostUndelete activityThread = new PostUndelete(db, doc, user);
        activityThread.setPriority(Thread.MIN_PRIORITY);
        activityThread.start();
        return 1;
    }

    @Override
    public StringBuffer getUsersWhichRead(int docID, int docType) {
        StringBuffer xmlContent = new StringBuffer(10000);

        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * FROM USERS_ACTIVITY WHERE TYPE=" + UsersActivityType.MARKED_AS_READ.getCode() + " AND DOCID=" + docID + " AND DOCTYPE=" + docType;
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                xmlContent.append("<entry eventtime=\"" + Database.dateTimeFormat.format(rs.getTimestamp("EVENTTIME")) + "\" userid=\"" + rs.getString("USERID") + "\"/>");
            }
            s.close();
            rs.close();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getUsersWhichRead(int docID, int docType, AppEnv env) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * FROM USERS_ACTIVITY WHERE TYPE=" + UsersActivityType.MARKED_AS_READ.getCode() + " AND DOCID=" + docID + " AND DOCTYPE=" + docType;
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                String userID = rs.getString("USERID");
                if (!Util.isGroupName(userID)) {
                    User user = new User(userID, env);
                    xmlContent.append("<entry eventtime=\"" + Database.dateTimeFormat.format(rs.getTimestamp("EVENTTIME")) + "\" username=\"" + user.getFullName() + "\"/>");
                }
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public int getAllActivityCount() {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(*) FROM USERS_ACTIVITY";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    @Override
    public int getActivityCount(int activityType, String userID) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(*) from USERS_ACTIVITY where userid = '" + userID + "' AND type = " + activityType;
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    @Override
    public StringBuffer getAllActivity(int offset, int pageSize) {
        StringBuffer xmlContent = new StringBuffer(10000);

        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * FROM USERS_ACTIVITY ORDER BY EVENTTIME LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("ID");
                UsersActivityType uat = UsersActivityType.getType(rs.getInt("TYPE"));
                xmlContent.append("<entry" + getDocumentAttrSet(rs) + " type=\"" + uat + "\">");
                if (uat == UsersActivityType.COMPOSED || uat == UsersActivityType.MODIFIED || uat == UsersActivityType.DELETED) {
                    Statement sFields = conn.createStatement();
                    String addSQL = "select * from USERS_ACTIVITY_CHANGES where AID = " + id;
                    ResultSet rsFields = sFields.executeQuery(addSQL);
                    xmlContent.append("<changes>");
                    while (rsFields.next()) {
                        xmlContent.append("<entry fieldname=\"" + rsFields.getString("FIELDNAME") + "\">" +
                                "<newvalue>" + XMLUtil.getAsTagValue(rsFields.getString("NEWVALUE")) + "</newvalue>" +
                                "<oldvalue>" + XMLUtil.getAsTagValue(rsFields.getString("OLDVALUE")) + "</oldvalue></entry>");
                    }
                    sFields.close();
                    xmlContent.append("</changes>");
                }
                xmlContent.append("</entry>");
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getActivity(int docID, int docType) {
        StringBuffer xmlContent = new StringBuffer(10000);

        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * FROM USERS_ACTIVITY WHERE DOCID=" + docID + " AND DOCTYPE=" + docType + " ORDER BY EVENTTIME";
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("ID");
                UsersActivityType uat = UsersActivityType.getType(rs.getInt("TYPE"));
                xmlContent.append("<entry" + getDocumentAttrSet(rs) + " type=\"" + uat + "\">");
                if (uat == UsersActivityType.COMPOSED || uat == UsersActivityType.MODIFIED || uat == UsersActivityType.DELETED) {
                    Statement sFields = conn.createStatement();
                    String addSQL = "select * from USERS_ACTIVITY_CHANGES where AID = " + id;
                    ResultSet rsFields = sFields.executeQuery(addSQL);
                    xmlContent.append("<changes>");
                    while (rsFields.next()) {
                        xmlContent.append("<entry fieldname=\"" + rsFields.getString("FIELDNAME") + "\">" +
                                "<newvalue>" + XMLUtil.getAsTagValue(rsFields.getString("NEWVALUE")) + "</newvalue>" +
                                "<oldvalue>" + XMLUtil.getAsTagValue(rsFields.getString("OLDVALUE")) + "</oldvalue></entry>");
                    }
                    sFields.close();
                    xmlContent.append("</changes>");
                }
                xmlContent.append("</entry>");
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getActivity(String userID) {
        StringBuffer xmlContent = new StringBuffer(10000);

        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * FROM USERS_ACTIVITY WHERE USERID='" + userID + "' ORDER BY EVENTTIME";
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("ID");
                UsersActivityType uat = UsersActivityType.getType(rs.getInt("TYPE"));
                xmlContent.append("<entry" + getDocumentAttrSet(rs) + " type=\"" + uat + "\">");
                if (uat == UsersActivityType.COMPOSED || uat == UsersActivityType.MODIFIED || uat == UsersActivityType.DELETED) {
                    Statement sFields = conn.createStatement();
                    String addSQL = "select * from USERS_ACTIVITY_CHANGES where AID = " + id;
                    ResultSet rsFields = sFields.executeQuery(addSQL);
                    xmlContent.append("<changes>");
                    while (rsFields.next()) {
                        xmlContent.append("<entry fieldname=\"" + rsFields.getString("FIELDNAME") + "\">" +
                                "<newvalue>" + XMLUtil.getAsTagValue(rsFields.getString("NEWVALUE")) + "</newvalue>" +
                                "<oldvalue>" + XMLUtil.getAsTagValue(rsFields.getString("OLDVALUE")) + "</oldvalue></entry>");
                    }
                    sFields.close();
                    xmlContent.append("</changes>");
                }
                xmlContent.append("</entry>");
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getActivity(String userID, int offset, int pageSize, int... typeCodes) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (typeCodes.length != 0) {
                for (int type : typeCodes) {
                    UsersActivityType activityType = UsersActivityType.getType(type);
                    String sql = "SELECT * FROM USERS_ACTIVITY WHERE USERID ='" + userID + "' AND TYPE = " + activityType.getCode() + " LIMIT " + pageSize + " OFFSET " + offset;
                    if (activityType == UsersActivityType.DELETED) {
                        sql = "SELECT ua.id as id, type, dbid, userid, eventtime, rb.id as docid, ddbid, clientip, " + Const.DOCTYPE_RECYCLE_BIN_ENTRY + " as doctype, viewtext from recycle_bin as rb" +
                                " left join users_activity as ua on rb.aid = ua.id " + " LIMIT " + pageSize + " OFFSET " + offset;
                    }
                    ResultSet rs = s.executeQuery(sql);
                    while (rs.next()) {
                        xmlContent.append("<entry" + getDocumentAttrSet(rs) + " type=\"" + activityType + "\">");
                        if (activityType == UsersActivityType.COMPOSED || activityType == UsersActivityType.MODIFIED) {
                            Statement sFields = conn.createStatement();
                            String addSQL = "Select * from USERS_ACTIVITY_CHANGES where aid = " + rs.getInt("ID");
                            ResultSet rsFields = sFields.executeQuery(addSQL);
                            xmlContent.append("<changes>");
                            while (rsFields.next()) {
                                xmlContent.append("<entry fieldname=\"" + rsFields.getString("FIELDNAME") + "\">" +
                                        "<newvalue>" + XMLUtil.getAsTagValue(rsFields.getString("NEWVALUE")) + "</newvalue>" +
                                        "<oldvalue>" + XMLUtil.getAsTagValue(rsFields.getString("OLDVALUE")) + "</oldvalue></entry>");
                            }
                            sFields.close();
                            xmlContent.append("</changes>");
                        }
                        xmlContent.append("</entry>");
                    }
                    rs.close();
                }
            } else {

                String sql = "SELECT * FROM USERS_ACTIVITY WHERE USERID='" + userID + "' ORDER BY EVENTTIME LIMIT " + pageSize + " OFFSET " + offset;
                ResultSet rs = s.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt("ID");
                    UsersActivityType uat = UsersActivityType.getType(rs.getInt("TYPE"));
                    xmlContent.append("<entry" + getDocumentAttrSet(rs) + " type=\"" + uat + "\">");
                    if (uat == UsersActivityType.COMPOSED || uat == UsersActivityType.MODIFIED || uat == UsersActivityType.DELETED) {
                        Statement sFields = conn.createStatement();
                        String addSQL = "select * from USERS_ACTIVITY_CHANGES where AID = " + id;
                        ResultSet rsFields = sFields.executeQuery(addSQL);
                        xmlContent.append("<changes>");
                        while (rsFields.next()) {
                            xmlContent.append("<entry fieldname=\"" + rsFields.getString("FIELDNAME") + "\">" +
                                    "<newvalue>" + XMLUtil.getAsTagValue(rsFields.getString("NEWVALUE")) + "</newvalue>" +
                                    "<oldvalue>" + XMLUtil.getAsTagValue(rsFields.getString("OLDVALUE")) + "</oldvalue></entry>");
                        }
                        sFields.close();
                        xmlContent.append("</changes>");
                    }
                    xmlContent.append("</entry>");
                }

            }
            s.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getActivityByService(String serviceName, int offset, int pageSize, int... typeCodes) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            for (int type : typeCodes) {
                UsersActivityType activityType = UsersActivityType.getType(type);
                String sql = "SELECT * FROM USERS_ACTIVITY WHERE DBID ='" + serviceName + "' AND TYPE = " + activityType.getCode() + " LIMIT " + pageSize + " OFFSET " + offset;
                if (activityType == UsersActivityType.DELETED) {
                    sql = "SELECT ua.id as id, type, dbid, userid, eventtime, rb.id as docid, " + Const.DOCTYPE_RECYCLE_BIN_ENTRY + " as doctype, viewtext from recycle_bin as rb" +
                            " left join users_activity as ua on rb.aid = ua.id " + " LIMIT " + pageSize + " OFFSET " + offset;
                }
                ResultSet rs = s.executeQuery(sql);
                while (rs.next()) {
                    xmlContent.append("<entry" + getDocumentAttrSet(rs) + " type=\"" + activityType + "\">");
                    if (activityType == UsersActivityType.COMPOSED || activityType == UsersActivityType.MODIFIED) {
                        Statement sFields = conn.createStatement();
                        String addSQL = "Select * from USERS_ACTIVITY_CHANGES where aid = " + rs.getInt("ID");
                        ResultSet rsFields = sFields.executeQuery(addSQL);
                        xmlContent.append("<changes>");
                        while (rsFields.next()) {
                            xmlContent.append("<entry fieldname=\"" + rsFields.getString("FIELDNAME") + "\">" +
                                    "<newvalue>" + XMLUtil.getAsTagValue(rsFields.getString("NEWVALUE")) + "</newvalue>" +
                                    "<oldvalue>" + XMLUtil.getAsTagValue(rsFields.getString("OLDVALUE")) + "</oldvalue></entry>");
                        }
                        sFields.close();
                        xmlContent.append("</changes>");
                    }
                    xmlContent.append("</entry>");
                }
                rs.close();
            }
            s.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public ViewEntryCollection getActivities(int offset, int pageSize, String userID, int... typeCodes) {
        HashSet<IViewEntry> activityEntries = new HashSet<IViewEntry>();
        ViewEntryCollection col = null;
        int count = 0;
        Connection conn = dbPool.getConnection();
        int[] a = new int[]{};
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            for (int type : typeCodes) {
                UsersActivityType activityType = UsersActivityType.getType(type);
                String sql = "SELECT * FROM USERS_ACTIVITY WHERE USERID ='" + userID + "' AND TYPE = " + activityType.getCode() + " LIMIT " + pageSize + " OFFSET " + offset;
                if (activityType == UsersActivityType.DELETED) {
                    sql = "SELECT ua.id as id, type, dbid, userid, eventtime, rb.id as docid, " + Const.DOCTYPE_RECYCLE_BIN_ENTRY + " as doctype, viewtext from recycle_bin as rb" +
                            " left join users_activity as ua on rb.aid = ua.id " + " LIMIT " + pageSize + " OFFSET " + offset;
                }
                ResultSet rs = s.executeQuery(sql);
                while (rs.next()) {
                    ViewEntry entry = new ViewEntry(db, rs, ViewEntryType.ACTIVITY);
                    activityEntries.add(entry);
                }
                rs.close();
            }

            for (int type : typeCodes) {
                UsersActivityType activityType = UsersActivityType.getType(type);
                String sql = "SELECT count(*) FROM USERS_ACTIVITY WHERE USERID ='" + userID + "' AND TYPE = " + activityType.getCode();
                if (activityType == UsersActivityType.DELETED) {
                    sql = "SELECT count(ua.id) from recycle_bin as rb" +
                            " left join users_activity as ua on rb.aid = ua.id";
                }
                ResultSet rs = s.executeQuery(sql);
                if (rs.next()) {
                    count += rs.getInt(1);
                }

            }

            s.close();
            conn.commit();
            col = new ViewEntryCollection(activityEntries, pageSize, db.getParent());
            col.setCount(count);
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return col;
    }

    @Override
    public int getActivitiesCount(String userID, int... typeCodes) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            for (int type : typeCodes) {
                UsersActivityType activityType = UsersActivityType.getType(type);
                String sql = "SELECT count(*) FROM USERS_ACTIVITY WHERE USERID ='" + userID + "' AND TYPE = " + activityType.getCode();
                if (activityType == UsersActivityType.DELETED) {
                    sql = "SELECT count(ua.id) from recycle_bin as rb" +
                            " left join users_activity as ua on rb.aid = ua.id";
                }
                ResultSet rs = s.executeQuery(sql);
                if (rs.next()) {
                    count += rs.getInt(1);
                }
                rs.close();
            }
            s.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public Document getRecycleBinEntry(int docID, Set<String> complexUserID, String absoluteUserID) {
        Document entry = new Document(db, absoluteUserID);
        String sql = "SELECT * FROM USERS_ACTIVITY WHERE ID = (SELECT AID FROM RECYCLE_BIN WHERE ID = " + docID + ")";
        Connection conn = dbPool.getConnection();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                entry.setDocID(docID);
                entry.docType = Const.DOCTYPE_RECYCLE_BIN_ENTRY;
                entry.addNumberField("activity_id", rs.getInt("ID"));
            }
            conn.commit();
            rs.close();
            st.close();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return entry;
    }

    private String getDocumentAttrSet(ResultSet rs) throws SQLException {
        Employer initiator = db.getStructure().getAppUser(rs.getString("userid"));
        return " dbid=\"" + rs.getString("DBID") + "\" ddbid=\"" + rs.getString("DDBID") + "\"" +
                " doctype=\"" + rs.getString("DOCTYPE") + "\" docid=\"" + rs.getString("DOCID") + "\"" +
                " eventtime=\"" + Database.dateTimeFormat.format(rs.getTimestamp("EVENTTIME")) + "\" " +
                " id =\"" + rs.getInt("ID") + "\"" +
                " ip =\"" + rs.getString("CLIENTIP") + "\"" +
                XMLUtil.getAsAttribute("viewtext", rs.getString("VIEWTEXT") != null ? rs.getString("VIEWTEXT") : "") +
                " userid=\"" + rs.getString("USERID") + "\"" +
                (initiator != null ? " initfio =\"" + initiator.getFullName() + "\"" : "");
    }

    @Override
    public int postSomeActivity(String event, User user) {
        int key = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String serviceName;

            int beginName = event.indexOf("[");
            int endName = event.indexOf("]", beginName);
            if (beginName > -1 && endName > -1) {
                serviceName = event.substring(event.indexOf("[") + 1, event.indexOf("]"));
            } else {
                serviceName = event;
            }

            event = event.replaceFirst("\\[.*\\]", "");

            String sql = "INSERT INTO USERS_ACTIVITY(TYPE, DBID, USERID, EVENTTIME, DOCID, DOCTYPE, VIEWTEXT, CLIENTIP) values ("
                    + UsersActivityType.CUSTOM_EVENT.getCode() + ", '" + serviceName + "','" + user.getUserID() + "','" + Database.sqlDateTimeFormat.format(new java.util.Date())
                    + "', " + 0 + ", " + Const.DOCTYPE_UNKNOWN + ", '" + event + "', '" + user.getSession().ipAddr + "')";
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            conn.commit();
            s.close();
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

}
