package kz.flabs.dataengine.h2.triggers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.api.Trigger;

public class AttachmentCounter implements Trigger {

    private String tableName;
    private int fireReason;

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void fire(Connection conn,
                     Object[] oldRow, Object[] newRow)
            throws SQLException {
        Statement sql = conn.createStatement();
        int docID = 0;
        if (fireReason == Trigger.INSERT)
            docID = (Integer) newRow[1];
        else
            docID = (Integer) oldRow[1];
        String idColName = "";
        String mainTableName = tableName.substring(tableName.lastIndexOf("_") + 1, tableName.length());
        if (tableName.equals("CUSTOM_BLOBS_BOSS") || tableName.equals("CUSTOM_BLOBS_EMPLOYERS"))
            idColName = "ID";
        else
            idColName = "DOCID";
        sql.executeUpdate("UPDATE " + mainTableName +
                " SET HAS_ATTACHMENT = (SELECT COUNT(*) FROM CUSTOM_BLOBS_" + mainTableName +
                " WHERE CUSTOM_BLOBS_" + mainTableName + ".DOCID = " + Integer.toString(docID) + ")" +
                " WHERE " + mainTableName + "." + idColName + " = " + Integer.toString(docID));
    }

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName,
                     boolean before, int type) throws SQLException {
        this.tableName = tableName;
        fireReason = type;
    }

    @Override
    public void remove() throws SQLException {

    }

}
