package kz.flabs.dataengine.postgresql.ftengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.FTIndexEngineExceptionType;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.User;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.dataengine.jpadatabase.ftengine.FTEntity;
import kz.lof.scripting._Session;
import kz.nextbase.script._ViewEntryCollection;

public class FTIndexEngine implements IFTIndexEngine, Const {
	private IDatabase db;
	private IDBConnectionPool dbPool;

	public FTIndexEngine(IDatabase db) {
		this.db = db;
		// this.dbPool = db.getConnectionPool();
	}

	public String normalizeKeywordToTSQuery(String keyword) {
		// String _keyWord = keyword.trim().replaceAll("\\s+{2}",
		// " ").replace(" ", " & ").replace(":", "\\:");

		return keyword;
	}

	public _ViewEntryCollection search(String keyWord, User user, int pageNum, int pageSize, String[] filters, String[] sorting)
	        throws FTIndexEngineException {
		HashSet<String> userGroups = user.getAllUserGroups();
		String userName = user.getUserID();
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, new String[4], new String[4]);
		String cuID = DatabaseUtil.prepareListToQuery(userGroups);
		String filterCondition = getFilterCondition(filters);
		String fields = "docid, doctype, ddbid, form, has_attachment, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			Document doc = null;
			int key;
			String table;
			String _keyWord = normalizeKeywordToTSQuery(keyWord);

			String sql = "SELECT distinct "
			        + fields
			        + " FROM maindocs, plainto_tsquery('"
			        + _keyWord
			        + "') q where fts @@ q \n"
			        + (userGroups.contains("[observer]") || userGroups.contains("[supervisor]") ? ""
			                : " and docid in (select docid from readers_maindocs where username IN (" + cuID + "))")
			        + (filterCondition.length() != 0 ? " and " + filterCondition : "")
			        + " union \n"
			        + "SELECT distinct "
			        + fields
			        + " from maindocs where docid in ("
			        + "SELECT id as docid FROM custom_fields, plainto_tsquery('"
			        + _keyWord
			        + "') q where fts @@ q) \n"
			        + (userGroups.contains("[observer]") || userGroups.contains("[supervisor]") ? ""
			                : " and docid in (select docid from readers_maindocs where username  IN (" + cuID + ") )")
			        + (filterCondition.length() != 0 ? " and " + filterCondition : "") +
			        /*
					 * " union \n" + "SELECT distinct " + fields +
					 * " FROM tasks, plainto_tsquery('" + _keyWord +
					 * "') q where fts @@ q \n" + (filterCondition.length() != 0
					 * ? " and " + filterCondition : "" ) +
					 * " and docid in (select docid from readers_tasks where username  IN ("
					 * + cuID + "))" + " union \n" + "SELECT distinct " + fields
					 * + " FROM executions, plainto_tsquery('" + _keyWord +
					 * "') q where fts @@ q \n" + (filterCondition.length() != 0
					 * ? " and " + filterCondition : "" ) +
					 * " and docid in (select docid from readers_executions where username  IN ("
					 * + cuID + "))" + " union \n" + "SELECT distinct " + fields
					 * + " FROM projects, plainto_tsquery('" + _keyWord +
					 * "') q where fts @@ q \n" + (filterCondition.length() != 0
					 * ? " and " + filterCondition : "" ) +
					 * " and docid in (select docid from readers_projects where username  IN ("
					 * + cuID + ")) " +
					 */
			        getOrderCondition(sorting) + " " + getPagingCondition(pageSize, pageNum) + ";";

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = null;
			try {
				rs = pst.executeQuery();
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, keyWord);
			}

			while (rs.next()) {
				ViewEntry entry = new ViewEntry(db, rs, new HashSet<DocID>(), user);
				// coll.add(entry);
			}

			sql = "SELECT count(DISTINCT total) from (SELECT distinct docid FROM maindocs, plainto_tsquery('"
			        + _keyWord
			        + "') q where fts @@ q \n"
			        + (userGroups.contains("[observer]") || userGroups.contains("[supervisor]") ? ""
			                : " and docid in (select docid from readers_maindocs where username IN (" + cuID + ")) ")
			        + (filterCondition.length() != 0 ? " and " + filterCondition : "")
			        + "union \n"
			        + "SELECT distinct docid from maindocs where docid in (SELECT id as docid FROM custom_fields, plainto_tsquery('"
			        + _keyWord
			        + "') q where fts @@ q) \n"
			        + (userGroups.contains("[observer]") || userGroups.contains("[supervisor]") ? ""
			                : " and docid in (select docid from readers_maindocs where username  IN (" + cuID + "))")
			        + (filterCondition.length() != 0 ? " and " + filterCondition : "") + ") as total;";

			/*
			 * sql =
			 * "SELECT SUM(count) from (SELECT count(distinct docid), 'maindocs' as tablename FROM maindocs, plainto_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_maindocs where username IN ("
			 * + cuID + "))" + " union \n" +
			 * "SELECT count(distinct docid), 'custom_fields' as tablename FROM custom_fields, plainto_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_maindocs where username IN ("
			 * + cuID + ")) and \n" +
			 * "  docid not in (SELECT distinct docid FROM maindocs, plainto_tsquery('"
			 * + _keyWord + "') q where fts @@ q)" + " union \n" +
			 * "SELECT count(distinct docid), 'tasks' as tablename FROM tasks, plainto_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_tasks where username IN ("
			 * + cuID + "))" + " union \n" +
			 * "SELECT count(distinct docid), 'executions' as tablename FROM executions, plainto_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_executions where username IN ("
			 * + cuID + "))" + " union \n" +
			 * "SELECT count(distinct docid), 'projects' as tablename FROM projects, plainto_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_projects where username IN ("
			 * + cuID + "))) as count;";
			 */
			pst = conn.prepareStatement(sql);
			try {
				rs = pst.executeQuery();
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, keyWord);
			}

			if (rs.next()) {
				coll.setCount(rs.getInt(1));
			}

			conn.commit();
			pst.close();
			rs.close();
		} catch (SQLException e) {
			// DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}

		if (pageNum == 0) {
			pageNum = RuntimeObjUtil.countMaxPage(coll.getCount(), pageSize);
		}
		coll.setCurrentPage(pageNum);

		return coll.getScriptingObj();

	}

	public StringBuffer ftSearch(Set<String> complexUserID, String absoluteUserID, String keyWord, int offset, int pageSize)
	        throws DocumentException, FTIndexEngineException, ComplexObjectException {
		StringBuffer xmlContent = new StringBuffer(10000);
		Set<String> set = new HashSet<String>();

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			Document doc = null;
			int key;
			String table;
			String _keyWord = normalizeKeywordToTSQuery(keyWord);

			String sql = "SELECT distinct docid, 'maindocs' as tablename FROM maindocs, plainto_tsquery('" + _keyWord + "') q where fts @@ q \n"
			        + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "')" + " union \n"
			        + "SELECT id as docid, 'custom_fields' as tablename FROM custom_fields, plainto_tsquery('" + _keyWord + "') q where fts @@ q \n"
			        + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "' and \n"
			        + "  docid not in (SELECT distinct docid FROM maindocs, plainto_tsquery('" + _keyWord + "') q where fts @@ q))" + " union \n"
			        + "SELECT distinct docid, 'tasks' as tablename FROM tasks, plainto_tsquery('" + _keyWord + "') q where fts @@ q \n"
			        + " and docid in (select docid from readers_tasks where username = '" + absoluteUserID + "')" + " union \n"
			        + "SELECT distinct docid, 'executions' as tablename FROM executions, plainto_tsquery('" + _keyWord + "') q where fts @@ q \n"
			        + " and docid in (select docid from readers_executions where username = '" + absoluteUserID + "')" + " union \n"
			        + "SELECT distinct docid, 'projects' as tablename FROM projects, plainto_tsquery('" + _keyWord + "') q where fts @@ q \n"
			        + " and docid in (select docid from readers_projects where username = '" + absoluteUserID + "') " + "offset " + offset
			        + " limit " + pageSize + ";";

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = null;
			try {
				rs = pst.executeQuery();
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, keyWord);
			}

			while (rs.next()) {
				doc = null;
				key = rs.getInt("docid");
				table = rs.getString("tablename");

				if (doc != null && set.add(doc.docType + " " + doc.getDocID())) {
					xmlContent.append(doc.toXMLEntry(""));
				}
			}
			conn.commit();
			pst.close();
			rs.close();
		} catch (SQLException e) {
			// DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}

		return xmlContent;
	}

	public int ftSearchCount(Set<String> complexUserID, String absoluteUserID, String keyWord) throws DocumentException {
		int count = 0;

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			// Document doc = null;
			// int key;
			// String table;
			String _keyWord = normalizeKeywordToTSQuery(keyWord);

			String sql = "SELECT count(distinct docid), 'maindocs' as tablename FROM maindocs, plainto_tsquery('" + _keyWord
			        + "') q where fts @@ q \n" + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "')"
			        + " union \n" + "SELECT count(distinct docid), 'custom_fields' as tablename FROM custom_fields, plainto_tsquery('" + _keyWord
			        + "') q where fts @@ q \n" + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID
			        + "') and \n" + "  docid not in (SELECT distinct docid FROM maindocs, plainto_tsquery('" + _keyWord + "') q where fts @@ q)"
			        + " union \n" + "SELECT count(distinct docid), 'tasks' as tablename FROM tasks, plainto_tsquery('" + _keyWord
			        + "') q where fts @@ q \n" + " and docid in (select docid from readers_tasks where username = '" + absoluteUserID + "')"
			        + " union \n" + "SELECT count(distinct docid), 'executions' as tablename FROM executions, plainto_tsquery('" + _keyWord
			        + "') q where fts @@ q \n" + " and docid in (select docid from readers_executions where username = '" + absoluteUserID + "')"
			        + " union \n" + "SELECT count(distinct docid), 'projects' as tablename FROM projects, plainto_tsquery('" + _keyWord
			        + "') q where fts @@ q \n" + " and docid in (select docid from readers_projects where username = '" + absoluteUserID + "');";

			/*
			 * String sql =
			 * "SELECT distinct docid, 'maindocs' as tablename FROM maindocs, to_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_maindocs where username = '"
			 * + absoluteUserID +"')" + " union \n" +
			 * "SELECT id as docid, 'custom_fields' as tablename FROM custom_fields, to_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_maindocs where username = '"
			 * + absoluteUserID +"' and " +
			 * "  docid not in (SELECT distinct docid FROM maindocs, to_tsquery('"
			 * + _keyWord + "') q where fts @@ q))" + " union \n" +
			 * "SELECT distinct docid, 'tasks' as tablename FROM tasks, to_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_tasks where username = '"
			 * + absoluteUserID +"')" + " union \n" +
			 * "SELECT distinct docid, 'executions' as tablename FROM executions, to_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_executions where username = '"
			 * + absoluteUserID +"')" + " union \n" +
			 * "SELECT distinct docid, 'projects' as tablename FROM projects, to_tsquery('"
			 * + _keyWord + "') q where fts @@ q \n" +
			 * " and docid in (select docid from readers_projects where username = '"
			 * + absoluteUserID +"');";
			 */

			// System.out.println(sql);

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				count += rs.getInt("count");
			}

			/*
			 * while (rs.next()) { doc = null; key = rs.getInt("docid"); table =
			 * rs.getString("tablename");
			 * 
			 * if (table.equalsIgnoreCase("custom_fields")){ sql =
			 * "SELECT DOCID FROM CUSTOM_FIELDS WHERE ID = " + key;
			 * PreparedStatement pst1 = conn.prepareStatement(sql); ResultSet
			 * rs1 = pst1.executeQuery(); if (rs1.next()) { int docID =
			 * rs1.getInt("DOCID"); try { doc = db.getMainDocumentByID(docID,
			 * complexUserID, absoluteUserID); count ++ ; } catch
			 * (DocumentAccessException e) { DatabaseUtil.errorPrint(e); } }
			 * pst1.close(); }else if (table.equalsIgnoreCase("maindocs")){ try
			 * { doc = db.getMainDocumentByID(key, complexUserID,
			 * absoluteUserID); count ++ ; } catch (DocumentAccessException e) {
			 * DatabaseUtil.errorPrint(e); } }else if
			 * (table.equalsIgnoreCase("tasks")){ try { doc =
			 * db.getTasks().getTaskByID(key, complexUserID, absoluteUserID);
			 * count ++ ; } catch (DocumentAccessException e) {
			 * DatabaseUtil.errorPrint(e); } }else if
			 * (table.equalsIgnoreCase("executions")){ try { doc =
			 * db.getExecutions().getExecutionByID(key, complexUserID,
			 * absoluteUserID); count ++ ; } catch (DocumentAccessException e) {
			 * DatabaseUtil.errorPrint(e); } }else if
			 * (table.equalsIgnoreCase("projects")){ try{ doc =
			 * db.getProjects().getProjectByID(key, complexUserID,
			 * absoluteUserID); count ++ ; } catch (DocumentAccessException e) {
			 * DatabaseUtil.errorPrint(e); } } }
			 */

			conn.commit();
			rs.close();
			pst.close();
		} catch (SQLException e) {
			// DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}

		return count;
	}

	private String getPagingCondition(int pageSize, int pageNum) {
		String pageSQL = "";
		if (pageSize > 0) {
			pageSQL += " LIMIT " + pageSize;
		}
		if (pageNum > 0) {

		}

		return pageSQL;

	}

	private String getOrderCondition(String[] sorting) {
		String tmpsql = "";
		for (int i = 0; i < sorting.length; i++) {
			String cond = sorting[i];
			boolean and = false;
			if (cond != null && !"".equalsIgnoreCase(cond) && !"null".equalsIgnoreCase(cond)) {
				and = false;
				switch (i) {
				case 0:
					cond = " VIEWTEXT " + cond;
					break;
				case 1:
				case 2:
				case 3:
					cond = " VIEWTEXT" + i + " " + cond;
					break;
				case 4:
					cond = " VIEWNUMBER " + cond;
					break;
				case 5:
					cond = " VIEWDATE " + cond;
					break;
				default:
					cond = " VIEWTEXT " + cond;
					break;
				}
				and = true;
			}
			if (and) {
				tmpsql += cond + ",";
			}
		}

		if (!"".equalsIgnoreCase(tmpsql)) {
			return " ORDER BY " + tmpsql.substring(0, tmpsql.length() - 1);
		} else {
			return " ORDER BY DOCID ASC";
		}
	}

	private String getFilterCondition(String[] filters) {
		String condition = "";
		for (int i = 0; i < filters.length; i++) {
			String temp = filters[i];
			boolean and = false;
			if (temp != null && !"".equalsIgnoreCase(temp) && !"null".equalsIgnoreCase(temp)) {
				and = false;
				switch (i) {
				case 0:
					temp = "VIEWTEXT LIKE '%" + temp + "%' ";
					break;
				case 1:
				case 2:
				case 3:
					temp = "VIEWTEXT" + i + " LIKE '%" + temp + "%' ";
					break;
				case 4:
					temp = "VIEWNUMBER = " + temp + " ";
					break;
				case 5:
					temp = "VIEWDATE = '" + temp + "' ";
					break;
				case 6:
					break;
				default:
					temp = "VIEWTEXT LIKE '%" + temp + "%'";
					break;
				}
				and = true;
			}
			if (and) {
				condition += temp;
				if (i < filters.length - 1) {
					condition += " and ";
				}
			}
		}
		return condition;
	}

	@Override
	public ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize) {
		return null;
	}

	@Override
	public void registerTable(FTEntity table) {
		// TODO Auto-generated method stub

	}
}
