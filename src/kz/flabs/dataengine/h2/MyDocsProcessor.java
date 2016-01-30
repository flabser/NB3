package kz.flabs.dataengine.h2;

import kz.flabs.dataengine.*;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.webrule.query.QueryFieldRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class MyDocsProcessor extends DatabaseCore implements IMyDocsProcessor, Const {
    private IDBConnectionPool dbPool;
    private IUsersActivity usersActivity;
    private IDatabase db;
    private String dbID;

    public MyDocsProcessor(IDatabase db) {
        usersActivity = db.getUserActivity();
        this.dbPool = db.getConnectionPool();
        this.db = db;
        dbID = db.getDbID();
    }

    public StringBuffer getTasksForMe(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, Set<DocID> toExpandResponses, int offset, int pageSize, QueryFieldRule[] fields) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            SortByBlock sortBlock = nf.getSortBlock();

			/*String sql = "select * from tasks where docid in (select docid from tasksexecutors where executor = '" + userName + "' " +
                    " intersect select docid from readers_tasks where username = '" + userName + "' " +
                    "except (select parentdocid from tasks where author = '" + userName + "' union select parentdocid from executions where author = '" + userName + "')) and ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;
		*/
            String sql = "select * from tasks where docid in (select docid from tasksexecutors where executor = '" + absoluteUserID + "' " +
                    " intersect select docid from readers_tasks where username = '" + absoluteUserID + "' " +
                    ") and ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;

            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
            sql += " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                int docID = rs.getInt("DOCID");
                StringBuffer value = new StringBuffer(1000);

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

                xmlContent.append(getEntry(conn, rs, absoluteUserID, value));
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    public int getTasksForMeCount(IQueryFormula nf, String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select count(*) from tasks where docid in (select docid from tasksexecutors where executor = '" + userName + "' " +
                    " intersect select docid from readers_tasks where username = '" + userName + "' " +
                    ") and ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getTasksForMeCount(String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select count(*) from tasks where docid in (select docid from tasksexecutors where executor = '" + userName +
                    "' except (select parentdocid from tasks where author = '" + userName +
                    "' union select parentdocid from executions where author = '" + userName +
                    "')) and ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;
            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                count = rs.getInt(1);
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    @Override
    public StringBuffer getMyTasks(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, Set<DocID> toExpandResponses, int offset, int pageSize, QueryFieldRule[] fields) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            SortByBlock sortBlock = nf.getSortBlock();
            String sql = "SELECT * FROM TASKS t, READERS_TASKS rt WHERE  t.DOCID = rt.DOCID and rt.USERNAME='" + absoluteUserID + "' " +
                    "and taskauthor = '" + absoluteUserID + "' and t.ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
            sql += " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                int docID = rs.getInt("DOCID");
                StringBuffer value = new StringBuffer(1000);

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

                xmlContent.append(getEntry(conn, rs, absoluteUserID, value));
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    public int getMyTasksCount(String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(t.docid) FROM TASKS t, READERS_TASKS rt WHERE  t.DOCID = rt.DOCID and rt.USERNAME='" + userName + "' " +
                    "and taskauthor = '" + userName + "' and t.ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;

            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                count = rs.getInt(1);
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getMyTasksCount(IQueryFormula nf, String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(t.docid) FROM TASKS t, READERS_TASKS rt WHERE  t.DOCID = rt.DOCID and rt.USERNAME='" + userName + "' " +
                    "and taskauthor = '" + userName + "' and t.ALLCONTROL=" + DatabaseConst.ALLCONTROL_ONCONTROL;
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                count = rs.getInt(1);
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }


    public StringBuffer getCompleteTask(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            SortByBlock sortBlock = nf.getSortBlock();

			/*String sql = "SELECT * FROM TASKS t, READERS_TASKS rt WHERE  t.DOCID = rt.DOCID and rt.USERNAME='" + userName	+ "' " +
                    "and taskauthor = '" + userName + "' and t.ALLCONTROL = " + DatabaseConst.ALLCONTROL_RESET + " ";*/

            String sql = "select * from tasks where docid in " +
                    "(select docid from tasksexecutors where executor = '" + userName + "' and resetdate is not null and resetauthor is not null " +
                    "intersect " +
                    "select docid from readers_tasks where username in ('" + userName + "')) " +
                    "and allcontrol = " + DatabaseConst.ALLCONTROL_RESET;

            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
            sql += " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                xmlContent.append(getEntry(conn, rs, userName, new StringBuffer(0)));

            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    public int getCompleteTaskCount(IQueryFormula nf, String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         /*   String sql = "SELECT count(t.docid) FROM TASKS t, READERS_TASKS rt WHERE  t.DOCID = rt.DOCID and rt.USERNAME='" + userName + "' " +
                    "and taskauthor = '" + userName + "' and t.ALLCONTROL = " + DatabaseConst.ALLCONTROL_RESET;*/

            String sql = "select count(docid) from tasks where docid in " +
                    "(select docid from tasksexecutors where executor = '" + userName + "' and resetdate is not null and resetauthor is not null " +
                    "intersect " +
                    "select docid from readers_tasks where username in ('" + userName + "')) " +
                    "and allcontrol = " + DatabaseConst.ALLCONTROL_RESET;

            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getCompleteTaskCount(String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(t.docid) FROM TASKS t, READERS_TASKS rt WHERE  t.DOCID = rt.DOCID and rt.USERNAME='" + userName + "' " +
                    "and taskauthor = '" + userName + "' and t.ALLCONTROL = " + DatabaseConst.ALLCONTROL_RESET;
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }


    public StringBuffer getPrjsWaitForCoord(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            SortByBlock sortBlock = nf.getSortBlock();
            String sql = "SELECT *, 0 as ALLCONTROL FROM PROJECTS p, READERS_PROJECTS rp, COORDBLOCKS cb, COORDINATORS c WHERE  p.DOCID = rp.DOCID and " +
                    "rp.USERNAME='" + userName + "' and p.COORDSTATUS = " + ICoordConst.STATUS_COORDINTING + " and c.COORDINATOR = '" + userName + "' and " +
                    "cb.DOCID = p.DOCID and cb.ID = c.BLOCKID and c.ISCURRENT = 1 ";
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
            sql += " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                xmlContent.append(getEntry(conn, rs, userName, new StringBuffer(0)));
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    public int getPrjsWaitForCoordCount(IQueryFormula nf, String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(p.docid) FROM PROJECTS p, READERS_PROJECTS rp, COORDBLOCKS cb, COORDINATORS c WHERE  p.DOCID = rp.DOCID and " +
                    "rp.USERNAME='" + userName + "' and p.COORDSTATUS = " + ICoordConst.STATUS_COORDINTING + " and c.COORDINATOR = '" + userName + "' and " +
                    "cb.DOCID = p.DOCID and cb.ID = c.BLOCKID and c.ISCURRENT = 1 ";
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getPrjsWaitForCoordCount(String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(p.docid) FROM PROJECTS p, READERS_PROJECTS rp, COORDBLOCKS cb, COORDINATORS c WHERE  p.DOCID = rp.DOCID and " +
                    "rp.USERNAME='" + userName + "' and p.COORDSTATUS = " + ICoordConst.STATUS_COORDINTING + " and c.COORDINATOR = '" + userName + "' and " +
                    "cb.DOCID = p.DOCID and cb.ID = c.BLOCKID and c.ISCURRENT = 1 ";

            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getPrjsWaitForSignCount(String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(*) FROM PROJECTS p, READERS_PROJECTS rp, COORDBLOCKS cb, COORDINATORS c WHERE  p.DOCID = rp.DOCID and " +
                    "rp.USERNAME='" + userName + "' and p.COORDSTATUS = " + ICoordConst.STATUS_SIGNING + " and c.COORDINATOR = '" + userName + "' and " +
                    "cb.DOCID = p.DOCID and cb.ID = c.BLOCKID and c.ISCURRENT = 1 ";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getPrjsWaitForSignCount(IQueryFormula nf, String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT count(*) FROM PROJECTS p, READERS_PROJECTS rp, COORDBLOCKS cb, COORDINATORS c WHERE  p.DOCID = rp.DOCID and " +
                    "rp.USERNAME='" + userName + "' and p.COORDSTATUS = " + ICoordConst.STATUS_SIGNING + " and c.COORDINATOR = '" + userName + "' and " +
                    "cb.DOCID = p.DOCID and cb.ID = c.BLOCKID and c.ISCURRENT = 1 ";
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public StringBuffer getPrjsWaitForSign(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            SortByBlock sortBlock = nf.getSortBlock();
            String sql = "SELECT *, 1 as ALLCONTROL FROM PROJECTS p, READERS_PROJECTS rp, COORDBLOCKS cb, COORDINATORS c WHERE  p.DOCID = rp.DOCID and " +
                    "rp.USERNAME='" + userName + "' and p.COORDSTATUS = " + ICoordConst.STATUS_SIGNING + " and c.COORDINATOR = '" + userName + "' and " +
                    "cb.DOCID = p.DOCID and cb.ID = c.BLOCKID and c.ISCURRENT = 1 ";
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " and " + key + " = " + conditions.get(key);
                }
            }
            sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
            sql += " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                xmlContent.append(getEntry(conn, rs, userName, new StringBuffer(0)));
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }


    public StringBuffer getToConsider(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            SortByBlock sortBlock = nf.getSortBlock();
            String sql = "select * from maindocs " +
                    " where maindocs.docid in (select docid from custom_fields where name = 'recipient' " +
                    " and value = '" + userName + "' except (select parentdocid from maindocs where parentdoctype = " + DOCTYPE_MAIN + " and " +
                    " author = '" + userName + "' union select parentdocid from maindocs where parentdoctype = " + DOCTYPE_MAIN + " and " +
                    " author = '" + userName + "') intersect (" +
                    " select docid from readers_maindocs where username = '" + userName + "')";


            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
                }
            }
            sql += " ) and form in ('IN', 'L', 'SZ') ";
            sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
            sql += " LIMIT " + pageSize + " OFFSET " + offset;
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                xmlContent.append(getEntry(conn, rs, userName, new StringBuffer(0)));

            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }


    public int getToConsiderCount(IQueryFormula nf, String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select count(*) from maindocs where docid in (select docid from custom_fields where name='recipient' " +
                    " and value = '" + userName + "' except (select parentdocid from maindocs where parentdoctype = " + DOCTYPE_MAIN + " and " +
                    " author = '" + userName + "' union select parentdocid from maindocs where parentdoctype = " + DOCTYPE_MAIN + " and " +
                    " author = '" + userName + "')";
            Filter quickFilter = nf.getQuickFilter();
            if (quickFilter != null && quickFilter.getEnable() == 1) {
                HashMap<String, String> conditions = nf.getQuickFilter().getConditions();
                for (String key : conditions.keySet()) {
                    sql += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
                }
            }
            sql += " ) and form in ('IN', 'L', 'SZ') ";

            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                count = rs.getInt(1);
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }

    public int getToConsiderCount(String userName) {
        int count = 0;
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select count(*) from maindocs where docid in (select docid from custom_fields where name='recipient' " +
                    " and value = '" + userName + "' except (select parentdocid from maindocs where parentdoctype = " + DOCTYPE_MAIN + " and " +
                    " author = '" + userName + "' union select parentdocid from maindocs where parentdoctype = " + DOCTYPE_MAIN + " and " +
                    " author = '" + userName + "')";
            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                count = rs.getInt(1);
            }

            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return count;
    }


    private String getHasResponseAttr(int docID, int docType, String userName) {
        Connection conn = dbPool.getConnection();

        try {
            conn.setAutoCommit(false);
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select * from TASKS t, READERS_TASKS r where t.PARENTDOCID ="
                    + docID + " and t.PARENTDOCTYPE =" + docType + " and r.DOCID = t.DOCID and r.USERNAME='" + userName + "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                st.close();
                return " hasresponse = \"1\" ";
            }

            sql = "select * from EXECUTIONS e, READERS_EXECUTIONS r where e.PARENTDOCID ="
                    + docID + " and e.PARENTDOCTYPE =" + docType + " and r.DOCID = e.DOCID and r.USERNAME='" + userName + "'";
            rs = st.executeQuery(sql);
            if (rs.next()) {
                st.close();
                return " hasresponse = \"1\" ";
            }

            rs.close();
            st.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return " hasresponse = \"0\" ";
    }

    public int randomBinary() {
        Random r = new Random();
        return r.nextInt(2);
    }

    private String getEntry(Connection conn, ResultSet rs, String userName, StringBuffer addValue) throws SQLException {
        int docID = rs.getInt("DOCID");
        String id = rs.getString("DDBID");
        int docType = rs.getInt("DOCTYPE");
        int allcontrol = 0;
        Employer emp =  db.getStructure().getAppUser(userName);
        
        return "<entry isread=\"" + usersActivity.isRead(conn, docID, docType, userName) + "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" id=\"" + rs.getString("DDBID") + "\" " +
                "doctype=\"" + docType + "\"" + getHasResponseAttr(docID, docType, userName) +
                "docid=\"" + docID + "\" " +
                "url=\"Provider?type=edit&amp;element=" + DatabaseUtil.resolveElement(docType) + "&amp;id=" + rs.getString("FORM") + "&amp;docid=" + id + "\" " +
                "allcontrol=\"" + allcontrol + "\" " +
                "favourites=\"" + db.isFavourites(conn, docID, docType, emp) + "\" " +
                ">" + getViewContent(rs) + addValue + "</entry>";
    }

}
