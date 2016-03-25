package kz.lof.dataengine.jpadatabase.ftengine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;

public class FTSearchEngineDeployer {
	private IDBConnectionPool dbPool;

	public FTSearchEngineDeployer(IDBConnectionPool dbPool) {
		this.dbPool = dbPool;
	}

	public void init() {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			Statement s = conn.createStatement();
			// s.execute(sql);
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}

	}
}
