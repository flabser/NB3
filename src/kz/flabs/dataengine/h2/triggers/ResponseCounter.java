package kz.flabs.dataengine.h2.triggers;


import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ResponseCounter implements Trigger {

    private String tableName;
    private int fireReason;

    @Override
    public void init(Connection connection, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
        this.tableName = tableName;
        fireReason = type;
    }

    @Override
    public void fire(Connection connection, Object[] oldRow, Object[] newRow) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " as ptn " +
                " SET ptn.HAS_RESPONSE = (SELECT COUNT(*) FROM " + tableName + " as ctn " +
                " WHERE ctn.PARENTDOCDDBID = ?) WHERE ptn.DDBID = ?");
        String ddbid = "";
        if (fireReason == Trigger.INSERT) {
            ddbid = (String) newRow[1];
        } else {
            ddbid = (String) oldRow[1];
        }
        statement.setString(1, ddbid);
        statement.setString(2, ddbid);
        statement.executeUpdate();
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void remove() throws SQLException {

    }
}
