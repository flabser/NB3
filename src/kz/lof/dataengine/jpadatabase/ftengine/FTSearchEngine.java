package kz.lof.dataengine.jpadatabase.ftengine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.lof.dataengine.jpa.IAppEntity;
import kz.lof.dataengine.jpa.IDAO;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;

public class FTSearchEngine implements IFTIndexEngine, Const {
	private IDBConnectionPool dbPool;
	private List<FTEntity> indexTables = new ArrayList<FTEntity>();
	private Class[] intArgsClass = new Class[] { _Session.class };

	public FTSearchEngine(IDatabase db) {
		this.dbPool = db.getConnectionPool();
	}

	// TODO It need improvement
	@Override
	public ViewPage<?> search(String keyWord, _Session ses, int pageNum, int pageSize) {
		Connection conn = dbPool.getConnection();
		String lang = getLangString(ses.getLang());
		try {
			conn.setAutoCommit(false);

			String sql = "";

			for (FTEntity table : indexTables) {
				sql = "SELECT id FROM " + table.getTableName() + " where to_tsvector('" + lang + "', " + table.getFieldNames().get(0)
				        + ") @@ to_tsquery('" + lang + "', '" + keyWord + "')";

				List<UUID> ids = new ArrayList<UUID>();
				PreparedStatement pst = conn.prepareStatement(sql);
				ResultSet rs = null;
				rs = pst.executeQuery();
				while (rs.next()) {
					ids.add(UUID.fromString(rs.getString("id")));
				}

				if (ids.size() > 0) {
					try {
						Constructor<?> constructor = table.getDaoImpl().getConstructor(intArgsClass);
						IDAO<? extends IAppEntity, UUID> dao = (IDAO<? extends IAppEntity, UUID>) constructor.newInstance(ses);
						ViewPage<?> vPage = dao.findAllByIds(ids, pageNum, pageSize);
						return vPage;
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					        | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}

				} else {
					return null;
				}
			}

		} catch (Exception pe) {
			System.out.println(pe);

		}
		return null;

	}

	@Override
	public void registerTable(FTEntity table) {
		indexTables.add(table);
	}

	private String getLangString(LanguageCode lang) {
		switch (lang) {
		case RUS:
			return "russian";
		case ENG:
			return "english";
		default:
			return "english";
		}

	}
}
