package kz.lof.dataengine.jpadatabase.ftengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentException;
import kz.flabs.users.User;
import kz.lof.dataengine.jpa.ViewPage;
import kz.nextbase.script._Session;
import kz.nextbase.script._ViewEntryCollection;

public class FTIndexEngine implements IFTIndexEngine, Const {
	private IDatabase db;
	private IDBConnectionPool dbPool;

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

			String sql = "SELECT id FROM properties where to_tsvector('russian', object_name) @@ to_tsquery('russian', '" + keyWord + "')";

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
	public _ViewEntryCollection search(String keyWord, User user, int pageNum, int pageSize, String[] filters, String[] sorting)
	        throws FTIndexEngineException {
		return null;
	}

	@Override
	@Deprecated
	public StringBuffer ftSearch(Set<String> complexUserID, String absoluteUserID, String keyWord, int offset, int pageSize)
	        throws DocumentException, FTIndexEngineException, ComplexObjectException {
		return null;

	}

	@Override
	@Deprecated
	public int ftSearchCount(Set<String> complexUserID, String absoluteUserID, String keyWord) throws DocumentException {
		return 0;
	}

	@Override
	public int updateFTIndex() throws FTIndexEngineException {
		return 0;
	}

}
