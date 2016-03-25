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

	@SuppressWarnings("SqlNoDataSourceInspection")
	public void init() {
		Connection conn = dbPool.getConnection();
		try (Statement s = conn.createStatement();){
			conn.setAutoCommit(false);

			String createDictionary = "" +
					"CREATE TEXT SEARCH DICTIONARY public.simple_dict (\n" +
					"   TEMPLATE = pg_catalog.simple,\n" +
					"   STOPWORDS = russian\n" +
					");";

			s.executeUpdate(createDictionary);
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
}
