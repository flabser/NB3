package kz.lof.dataengine.jpadatabase.ftengine;

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
import kz.lof.dataengine.jpa.ViewPage;
import kz.nextbase.script._Session;

public class FTIndexEngine implements IFTIndexEngine, Const {
	private IDatabase db;
	private IDBConnectionPool dbPool;
	private List<ToFTIndex> indexTables = new ArrayList<ToFTIndex>();

	public FTIndexEngine(IDatabase db) {
		this.db = db;
		this.dbPool = db.getConnectionPool();
	}

	// TODO It need improvement
	@Override
	public ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			String sql = "";

			for (ToFTIndex table : indexTables) {
				sql = "SELECT id FROM " + table.getTableName() + " where to_tsvector('" + table.getLang() + "', " + table.getFieldName()
				        + ") @@ to_tsquery('" + table.getLang() + "', '" + keyWord + "')";
			}

			List<UUID> ids = new ArrayList<UUID>();
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = null;
			rs = pst.executeQuery();
			while (rs.next()) {
				ids.add(UUID.fromString(rs.getString("id")));
			}

			if (ids.size() > 0) {
				// PropertyDAO dao = new PropertyDAO(ses);
				// ViewPage<Property> vPage = dao.findAllByIds(ids, pageNum,
				// pageSize);
				// return vPage;
			} else {
				return null;
			}

		} catch (Exception pe) {
			System.out.println(pe);

		}
		return null;

	}

	@Override
	public void registerTable(ToFTIndex table) {
		indexTables.add(table);
	}

}
