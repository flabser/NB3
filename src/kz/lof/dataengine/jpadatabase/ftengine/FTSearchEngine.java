package kz.lof.dataengine.jpadatabase.ftengine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

	// TODO It need to improve
	// работает при условии что FTEntity.fieldNames[0] является именем поля где нужно искать искомое слово
	@Override
	public List<ViewPage<?>> search(String keyWord, _Session ses, int pageNum, int pageSize) {
		Connection conn = dbPool.getConnection();
		String lang = getLangString(ses.getLang());
		List<ViewPage<?>> result = new ArrayList<>();
		try {
			conn.setAutoCommit(false);

			StringBuilder sql = new StringBuilder();
			for (FTEntity table : indexTables) {
				sql.append("SELECT '").append(table.getTableName()).append("' as table_name, id FROM ").append(table.getTableName()).append(" where to_tsvector('").append(lang).append("', ").append(table.getFieldNames().get(0)).append(") @@ to_tsquery('").append(lang).append("', '").append(keyWord).append("') union all ");
			}

			sql.append("SELECT 'EMPTY', '00000000-0000-0000-0000-000000000000'::uuid;");

			PreparedStatement pst = conn.prepareStatement(sql.toString());
			ResultSet rs = pst.executeQuery();

			List<UUID> ids = new ArrayList<>();
			String currentTableName = "";
			while (rs.next()) {
				String tableName = rs.getString("table_name");
				if(!currentTableName.equals(tableName)){
					final String finalCurrentTableName = currentTableName;
					Optional<FTEntity> table = indexTables.stream().filter(r -> r.getTableName().equals(finalCurrentTableName)).findFirst();

					if (ids.size() > 0 && table.isPresent()) {
						try {
							Constructor<?> constructor =       table.get().getDaoImpl().getConstructor(intArgsClass);
							IDAO<? extends IAppEntity, UUID> dao = (IDAO<IAppEntity, UUID>) constructor.newInstance(ses);
							ViewPage<?> vPage = dao.findAllByIds(ids, pageNum, pageSize);
							result.add(vPage);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
						}

					}

					currentTableName = tableName;
					ids.clear();
				}

				ids.add(UUID.fromString(rs.getString("id")));
			}


		} catch (Exception pe) {
			System.out.println(pe);
		} finally {
			dbPool.returnConnection(conn);
		}

		return result.size() > 0 ? result : null;

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
