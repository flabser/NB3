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
import java.util.stream.Collectors;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.lof.dataengine.jpa.IAppEntity;
import kz.lof.dataengine.jpa.IDAO;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;

import javax.swing.text.View;

public class FTSearchEngine implements IFTIndexEngine, Const {
	private IDBConnectionPool dbPool;
	private List<FTEntity> indexTables = new ArrayList<FTEntity>();
	private Class[] intArgsClass = new Class[] { _Session.class };

	public FTSearchEngine(IDatabase db) {
		this.dbPool = db.getConnectionPool();
	}

	@Override
	public ViewPage<?> search(String keyWord, _Session ses, int pageNum, int pageSize) {
		if(keyWord == null || keyWord.trim().isEmpty() || indexTables.isEmpty())
			return null;

		Connection conn = dbPool.getConnection();
		String lang = getLangString(ses.getLang());
		List result = new ArrayList<>();

		try {
			conn.setAutoCommit(false);

			StringBuilder sql = new StringBuilder();

			String tsVectorTemplate = "to_tsvector('" + lang + "', %s::character varying)";
			String sqlPart = "select '%s' as table_name, id from %s where %s @@ to_tsquery('" + lang + "', '" + keyWord + "') union all ";

			for (FTEntity table : indexTables) {
				String tsVectors = table.getFieldNames().stream().map(colName-> String.format(tsVectorTemplate, colName)).collect(Collectors.joining("||"));
				sql.append(String.format(sqlPart, table.getTableName(), table.getTableName(), tsVectors));
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
							result.addAll(vPage.getResult());
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

//		List res = new ArrayList<>();
//		result.stream().forEach(v -> res.addAll(v.getResult()));
//		ViewPage<?> tem = new ViewPage<>(res, 0, 0, 0);

		return result.size() > 0 ? new ViewPage<>(result, result.size(), pageNum, pageSize) : null;

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
