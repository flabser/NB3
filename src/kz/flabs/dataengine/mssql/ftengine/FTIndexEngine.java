package kz.flabs.dataengine.mssql.ftengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseConst;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.FTIndexEngineExceptionType;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.dataengine.jpa.ViewPage;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.nextbase.script._Session;
import kz.nextbase.script._ViewEntryCollection;

//import java.sql.Statement;

public class FTIndexEngine implements IFTIndexEngine, Const {
	private IDatabase db;
	private IDBConnectionPool dbPool;

	public FTIndexEngine(IDatabase db) {
		this.db = db;
		this.dbPool = db.getConnectionPool();
	}

	public String normalizeKeywordToTSQuery(String keyword) {
		// String _keyWord = keyword.trim().replaceAll("\\s+{2}",
		// " ").replace(" ", " & ").replace(":", "\\:");

		return keyword;
	}

	@Override
	public StringBuffer ftSearch(Set<String> complexUserID, String absoluteUserID, String keyWord, int offset, int pageSize)
	        throws DocumentException, FTIndexEngineException {
		StringBuffer xmlContent = new StringBuffer(10000);
		Set<String> set = new HashSet<String>();
		String _keyWord = "\"" + normalizeKeywordToTSQuery(keyWord) + "\"";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Document doc = null;
			int key;
			String table;
			/*
			 * String sql =
			 * "SELECT distinct docid, 'maindocs' as tablename FROM maindocs where contains (viewtext, '"
			 * + _keyWord + "') or contains (viewtext1, '" + _keyWord +
			 * "') or contains (viewtext2, '" + _keyWord +
			 * "') or contains (viewtext3, '" + _keyWord + "') \n" +
			 * " and docid in (select docid from readers_maindocs where username = '"
			 * + absoluteUserID +"')" + " union \n" +
			 * "SELECT id as docid, 'custom_fields' as tablename FROM custom_fields where contains (value, '"
			 * + _keyWord + "') \n" +
			 * " and docid in (select docid from readers_maindocs where username = '"
			 * + absoluteUserID +"' and \n" +
			 * "  docid not in (SELECT distinct docid FROM maindocs where contains (viewtext, '"
			 * + _keyWord + "') or contains (viewtext1, '" + _keyWord +
			 * "') or contains (viewtext2, '" + _keyWord +
			 * "') or contains (viewtext3, '" + _keyWord + "')))" + " union \n"
			 * +
			 * "SELECT distinct docid, 'tasks' as tablename FROM tasks where contains (viewtext, '"
			 * + _keyWord + "') or contains (viewtext1, '" + _keyWord +
			 * "') or contains (viewtext2, '" + _keyWord +
			 * "') or contains (viewtext3, '" + _keyWord + "') \n" +
			 * " and docid in (select docid from readers_tasks where username = '"
			 * + absoluteUserID +"')" + " union \n" +
			 * "SELECT distinct docid, 'executions' as tablename FROM executions where contains (viewtext, '"
			 * + _keyWord + "') or contains (viewtext1, '" + _keyWord +
			 * "') or contains (viewtext2, '" + _keyWord +
			 * "') or contains (viewtext3, '" + _keyWord + "') \n" +
			 * " and docid in (select docid from readers_executions where username = '"
			 * + absoluteUserID +"')" + " union \n" +
			 * "SELECT distinct docid, 'projects' as tablename FROM projects where contains (viewtext, '"
			 * + _keyWord + "') or contains (viewtext1, '" + _keyWord +
			 * "') or contains (viewtext2, '" + _keyWord +
			 * "') or contains (viewtext3, '" + _keyWord + "') \n" +
			 * " and docid in (select docid from readers_projects where username = '"
			 * + absoluteUserID +"') "
			 *//*
				 * + "offset " + offset + " limit " + pageSize + ";"
				 *//* ; */
			String[] tables = new String[] { "MAINDOCS", "TASKS", "EXECUTIONS", "PROJECTS" };
			String containsViewtext = "";
			for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
				containsViewtext += " or contains (viewtext" + i + ", '" + _keyWord + "') ";
			}
			String sql = " DECLARE @intStartRow int; " + " DECLARE @intEndRow int; " + " DECLARE @intPage int = " + offset + ";"
			        + " DECLARE @intPageSize int = " + pageSize + ";" + " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;"
			        + " SET @intEndRow = @intPage * @intPageSize;" + " WITH blogs AS" + " (SELECT  distinct docid, 'custom_fields' as tablename, "
			        + " ROW_NUMBER() OVER(ORDER BY ID " + Const.DEFAULT_SORT_ORDER + ") as intRow, " + " COUNT(ID) OVER() AS intTotalHits "
			        + " FROM CUSTOM_FIELDS " + " where contains (value, '" + _keyWord + "') \n"
			        + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "' and \n"
			        + " docid not in (SELECT distinct docid FROM maindocs where contains (viewtext, '" + _keyWord + "') " + containsViewtext + "))"
			        + " and docid in (select docid from readers_maindocs" + " where username = '" + absoluteUserID + "')" + " ) "
			        + " SELECT distinct docid, 'CUSTOM_FIELDS' as tablename FROM blogs" + " WHERE intRow BETWEEN @intStartRow AND @intEndRow";

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			processResultSet(conn, rs, xmlContent, complexUserID, absoluteUserID);
			for (String tableName : tables) {
				sql = " DECLARE @intStartRow int; " + " DECLARE @intEndRow int; " + " DECLARE @intPage int = " + offset + ";"
				        + " DECLARE @intPageSize int = " + pageSize + ";" + " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;"
				        + " SET @intEndRow = @intPage * @intPageSize;" + " WITH blogs AS" + " (SELECT  distinct docid, viewdate, '" + tableName
				        + "' as tablename, " + " ROW_NUMBER() OVER(ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER
				        + ") as intRow, " + " COUNT(" + Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " + " FROM " + tableName
				        + " where contains (viewtext, '" + _keyWord + "') " + containsViewtext + " \n"
				        + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "')" + " ) "
				        + " SELECT distinct docid, viewdate, '" + tableName + "' as tablename FROM blogs"
				        + " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
				/* " */
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				processResultSet(conn, rs, xmlContent, complexUserID, absoluteUserID);
			}
			conn.commit();
			pst.close();
			rs.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} catch (Exception pe) {
			throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, _keyWord);
		} finally {
			dbPool.returnConnection(conn);
		}

		return xmlContent;
	}

	private void processResultSet(Connection conn, ResultSet rs, StringBuffer xmlContent, Set<String> complexUserID, String absoluteUserID)
	        throws ComplexObjectException {
		Set<String> set = new HashSet<String>();
		try {
			while (rs.next()) {
				Document doc = null;
				int key = rs.getInt("docid");
				String table = rs.getString("tablename");
				String sql = "";
				try {
					if (table.equalsIgnoreCase("custom_fields")) {
						sql = "SELECT DOCID FROM CUSTOM_FIELDS WHERE ID = " + key;
						PreparedStatement pst1 = conn.prepareStatement(sql);
						ResultSet rs1 = pst1.executeQuery();
						if (rs1.next()) {
							doc = db.getMainDocumentByID(rs1.getInt("DOCID"), complexUserID, absoluteUserID);
						}
						pst1.close();
					} else if (table.equalsIgnoreCase("maindocs")) {
						doc = db.getMainDocumentByID(key, complexUserID, absoluteUserID);
					} else if (table.equalsIgnoreCase("tasks")) {
						doc = db.getTasks().getTaskByID(key, complexUserID, absoluteUserID);
					} else if (table.equalsIgnoreCase("executions")) {
						doc = db.getExecutions().getExecutionByID(key, complexUserID, absoluteUserID);
					} else if (table.equalsIgnoreCase("projects")) {
						doc = db.getProjects().getProjectByID(key, complexUserID, absoluteUserID);
					}
				} catch (DocumentAccessException e) {
					// xmlContent.append("<entry viewtext=\"access restricted\"></entry>");
				}
				if (doc != null && set.add(doc.docType + " " + doc.getDocID())) {
					xmlContent.append(doc.toXMLEntry(""));
				}
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} catch (DocumentException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
	}

	@Override
	public int ftSearchCount(Set<String> complexUserID, String absoluteUserID, String keyWord) throws DocumentException {
		int count = 0;

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String _keyWord = "\"" + normalizeKeywordToTSQuery(keyWord) + "\"";
			String containsViewtext = "";
			for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
				containsViewtext += " or contains (viewtext" + i + ", '" + _keyWord + "') ";
			}
			String sql = "SELECT count(distinct docid) as count, 'maindocs' as tablename FROM maindocs where contains (viewtext, '" + _keyWord
			        + "') " + containsViewtext + " \n" + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID
			        + "')" + " union \n"
			        + "SELECT count(distinct docid) as count, 'custom_fields' as tablename FROM custom_fields where contains (value, '" + _keyWord
			        + "') \n" + " and docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "') and \n"
			        + "  docid not in (SELECT distinct docid FROM maindocs where contains (viewtext, '" + _keyWord + "') " + containsViewtext + " )"
			        + " union \n" + "SELECT count(distinct docid) as count, 'tasks' as tablename FROM tasks where contains (viewtext, '" + _keyWord
			        + "') " + containsViewtext + " \n" + " and docid in (select docid from readers_tasks where username = '" + absoluteUserID + "')"
			        + " union \n" + "SELECT count(distinct docid) as count, 'executions' as tablename FROM executions where contains (viewtext, '"
			        + _keyWord + "') " + containsViewtext + " \n" + " and docid in (select docid from readers_executions where username = '"
			        + absoluteUserID + "')" + " union \n"
			        + "SELECT count(distinct docid) as count, 'projects' as tablename FROM projects where contains (viewtext, '" + _keyWord + "') "
			        + containsViewtext + "  \n" + " and docid in (select docid from readers_projects where username = '" + absoluteUserID + "');";

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				count += rs.getInt("count");
			}

			conn.commit();
			rs.close();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}

		return count;
	}

	@Override
	public int updateFTIndex() throws FTIndexEngineException {
		/*
		 * Connection conn = dbPool.getConnection(); try {
		 * conn.setAutoCommit(false); Statement stmt = conn.createStatement();
		 * try{ stmt.execute("CALL FTL_REINDEX()"); }catch(Exception pe){ throw
		 * new
		 * FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED,
		 * ""); } conn.commit(); stmt.close(); } catch (SQLException e) {
		 * DatabaseUtil.errorPrint(e); } finally {
		 * dbPool.returnConnection(conn); }
		 */

		return 0;
	}

	@Override
	public _ViewEntryCollection search(String keyWord, User user, int pageNum, int pageSize, String[] sorting, String[] filters)
	        throws FTIndexEngineException {
		return null;
	}

	@Override
	public ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

}
