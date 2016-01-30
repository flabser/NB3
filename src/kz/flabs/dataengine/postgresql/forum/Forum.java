package kz.flabs.dataengine.postgresql.forum;


import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.postgresql.Database;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.nextbase.script._ViewEntryCollection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Forum extends kz.flabs.dataengine.h2.forum.Forum {
    public Forum(Database database, IDBConnectionPool forumDbPool) {
        super(database, forumDbPool);
    }

    @Override
    public _ViewEntryCollection getTopicsCollection(ISelectFormula condition, User user, int pageNum, int pageSize, RunTimeParameters parameters, boolean checkResponse) {
        ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
        Set<String> users = user.getAllUserGroups();
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (pageNum == 0) {
                String sql = condition.getCountCondition(users, parameters.getFilters());
                ResultSet rs = s.executeQuery(sql);
                if (rs.next()) {
                    pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
                }
            }
            int offset = db.calcStartEntry(pageNum, pageSize);
            String sql = condition.getCondition(users, pageSize, offset, parameters.getFilters(), parameters.getSorting(), checkResponse);
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                ViewEntry entry = new ViewEntry(this.db, rs, new HashSet<DocID>(), user, parameters.getDateFormat());
                coll.add(entry);
                coll.setCount(rs.getInt(1));
                while (rs.next()) {
                    entry = new ViewEntry(this.db, rs, new HashSet<DocID>(), user, parameters.getDateFormat());
                    coll.add(entry);
                }
            }
            conn.commit();
            s.close();
            rs.close();

        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } catch (Exception e) {
            Database.logger.errorLogEntry(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        coll.setCurrentPage(pageNum);
        return coll.getScriptingObj();
    }
}
