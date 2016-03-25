package kz.lof.dataengine.jpadatabase.ftengine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;

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
			s.executeUpdate(createDict());
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	private String createDict(){

		String dictionaryTemplate = "" +
				"CREATE TEXT SEARCH DICTIONARY public.%s_dict (\n" +
				"   TEMPLATE = pg_catalog.simple,\n" +
				"   STOPWORDS = %s\n" +
				");";

		return Environment.langs.stream().map(el -> String.format(dictionaryTemplate, el.name(), el.getLang()))
				.reduce("", (acc, rec) -> acc + rec);
	}
}
