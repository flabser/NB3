package kz.flabs.dataengine.h2.ftengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kz.flabs.dataengine.Const;
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
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.User;
import kz.nextbase.script._Session;
import kz.nextbase.script._ViewEntryCollection;

public class FTIndexEngine implements IFTIndexEngine, Const {
	private IDatabase db;
	private IDBConnectionPool dbPool;
	private static String patternStr = "\\(|\\)";
	private static Pattern pattern = Pattern.compile(patternStr);
	private static String replacementStr = "";

	public FTIndexEngine(IDatabase db) {
		this.db = db;
		this.dbPool = db.getConnectionPool();
	}

	@Override
	public StringBuffer ftSearch(Set<String> complexUserID, String absoluteUserID, String keyWord, int offset, int pageSize)
	        throws DocumentException, FTIndexEngineException, ComplexObjectException {
		StringBuffer xmlContent = new StringBuffer(10000);
		Set<String> set = new HashSet<String>();

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "SELECT * FROM FTL_SEARCH_DATA('" + keyWord + "', " + pageSize + ", " + offset + ");";

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = null;
			try {
				rs = pst.executeQuery();
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, keyWord);
			}
			while (rs.next()) {
				Document doc = null;
				CharSequence inputStr = rs.getString("KEYS");
				Matcher matcher = pattern.matcher(inputStr);
				String output = matcher.replaceAll(replacementStr);
				int key = Integer.parseInt(output);
				String table = rs.getString("TABLE");
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
					} else if (table.equalsIgnoreCase("topics")) {
						doc = db.getForum().getTopicByID(key, complexUserID, absoluteUserID);
					} else if (table.equalsIgnoreCase("posts")) {
						doc = db.getForum().getPostByID(key, complexUserID, absoluteUserID);
					}

				} catch (DocumentAccessException e) {
					// xmlContent.append("<entry viewtext=\"access restricted\"></entry>");
				}
				if (doc != null && set.add(doc.docType + " " + doc.getDocID())) {
					xmlContent.append(doc.toXMLEntry(""));
				}

			}
			conn.commit();
			pst.close();
			rs.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);

		}
		return xmlContent;
	}

	@Override
	public int ftSearchCount(Set<String> complexUserID, String absoluteUserID, String keyWord) throws DocumentException, ComplexObjectException {
		int count = 0;

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "SELECT * FROM FTL_SEARCH_DATA('" + keyWord + "', 0, 0);";

			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				CharSequence inputStr = rs.getString("KEYS");
				Matcher matcher = pattern.matcher(inputStr);
				String output = matcher.replaceAll(replacementStr);
				int key = Integer.parseInt(output);
				String table = rs.getString("TABLE");
				if (table.equalsIgnoreCase("custom_fields")) {
					sql = "SELECT DOCID FROM CUSTOM_FIELDS WHERE ID = " + key;
					PreparedStatement pst1 = conn.prepareStatement(sql);
					ResultSet rs1 = pst1.executeQuery();
					if (rs1.next()) {
						int docID = rs1.getInt("DOCID");
						try {
							Document doc = db.getMainDocumentByID(docID, complexUserID, absoluteUserID);
							count++;
						} catch (DocumentAccessException e) {
							DatabaseUtil.errorPrint(db.getDbID(), e);
						}

					}
					pst1.close();
				} else if (table.equalsIgnoreCase("maindocs")) {
					try {
						Document doc = db.getMainDocumentByID(key, complexUserID, absoluteUserID);
						count++;
					} catch (DocumentAccessException e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
				} else if (table.equalsIgnoreCase("tasks")) {
					try {
						Document doc = db.getTasks().getTaskByID(key, complexUserID, absoluteUserID);
						count++;
					} catch (DocumentAccessException e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
				} else if (table.equalsIgnoreCase("executions")) {
					try {
						Document doc = db.getExecutions().getExecutionByID(key, complexUserID, absoluteUserID);
						count++;
					} catch (DocumentAccessException e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
				} else if (table.equalsIgnoreCase("projects")) {
					try {
						Document doc = db.getProjects().getProjectByID(key, complexUserID, absoluteUserID);
						count++;
					} catch (DocumentAccessException e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
				} else if (table.equalsIgnoreCase("topics")) {
					try {
						Document doc = db.getForum().getTopicByID(key, complexUserID, absoluteUserID);
						count++;
					} catch (DocumentAccessException e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
				} else if (table.equalsIgnoreCase("posts")) {
					try {
						Document doc = db.getForum().getPostByID(key, complexUserID, absoluteUserID);
						count++;
					} catch (DocumentAccessException e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
				}
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
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			try {
				stmt.execute("CALL FTL_REINDEX()");
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.FT_ENGINE_ERROR, "");
			}
			conn.commit();
			stmt.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);

		}
		return 0;
	}

	private String getPagingCondition(int pageSize, int pageNum) {
		String pageSQL = "";
		if (pageSize > 0) {
			pageSQL += " LIMIT " + pageSize;
		}
		if (pageNum > 0) {
			pageSQL += " OFFSET " + this.db.calcStartEntry(pageNum, pageSize);
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
	public _ViewEntryCollection search(String keyWord, User user, int pageNum, int pageSize, String[] sorting, String[] filters)
	        throws FTIndexEngineException {
		HashSet<String> userGroups = user.getAllUserGroups();
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, new String[4], new String[4]);
		String cuID = DatabaseUtil.prepareListToQuery(userGroups);
		String filterCondition = getFilterCondition(filters);
		String fields = "mt.docid, mt.doctype, mt.ddbid, mt.form, mt.has_attachment, mt.viewtext, " + DatabaseUtil.getViewTextList("mt")
		        + ", mt.viewnumber, mt.viewdate";
		Set<String> set = new HashSet<>();

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			String sql = "SELECT * FROM FTL_SEARCH_DATA('" + keyWord + "', 0, 0);";
			PreparedStatement pst = conn.prepareStatement(sql);
			HashSet<DocID> result = new HashSet<>();
			ResultSet rs;
			try {
				rs = pst.executeQuery();
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, keyWord);
			}

			while (rs.next()) {
				String id = rs.getString(4).replaceAll("\\(|\\)", "");
				switch (rs.getString(2)) {
				case "CUSTOM_FIELDS":
					sql = "select docid from custom_fields where id = " + id;
					PreparedStatement pst_add = conn.prepareStatement(sql);
					ResultSet rs_add = pst_add.executeQuery();
					if (rs_add.next()) {
						result.add(new DocID(rs_add.getInt(1), Const.DOCTYPE_MAIN));
					}
					rs_add.close();
					pst_add.close();
					break;
				case "MAINDOCS":
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_MAIN));
					break;
				case "PROJECTS":
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_PROJECT));
					break;
				case "TASKS":
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_TASK));
					break;
				case "EXECUTIONS":
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_EXECUTION));
					break;
				case "POSTS":
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_POST));
					break;
				case "TOPICS":
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_TOPIC));
					break;
				default:
					result.add(new DocID(Integer.parseInt(id), Const.DOCTYPE_MAIN));
					break;
				}
			}
			coll.setCount(result.size());
			if (pageNum == 0) {
				pageNum = RuntimeObjUtil.countMaxPage(result.size(), pageSize);
			}
			sql = "SELECT * FROM FTL_SEARCH_DATA('" + keyWord + "', " + pageSize + ", " + this.db.calcStartEntry(pageNum, pageSize) + ");";
			pst = conn.prepareStatement(sql);
			rs = null;
			try {
				rs = pst.executeQuery();
			} catch (Exception pe) {
				throw new FTIndexEngineException(FTIndexEngineExceptionType.QUERY_UNRECOGNIZED, keyWord);
			}
			while (rs.next()) {
				CharSequence inputStr = rs.getString("KEYS");
				Matcher matcher = pattern.matcher(inputStr);
				String output = matcher.replaceAll(replacementStr);
				int key = Integer.parseInt(output);
				String table = rs.getString("TABLE");

				switch (table.toUpperCase()) {
				case "CUSTOM_FIELDS":
					sql = "SELECT distinct " + fields + " FROM maindocs as mt, readers_maindocs as rm, custom_fields as cf where exists "
					        + "(select * from readers_maindocs where readers_maindocs.docid = rm.docid and readers_maindocs.username in (" + cuID
					        + ")) and exists " + "(select * from custom_fields where custom_fields.docid = cf.docid and custom_fields.id = " + key
					        + ")" + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				case "MAINDOCS":
					sql = "SELECT distinct " + fields + " FROM maindocs as mt, readers_maindocs as rm where mt.docid = " + key + " and exists "
					        + "(select * from readers_maindocs where readers_maindocs.docid = rm.docid and readers_maindocs.username in (" + cuID
					        + "))" + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				case "TASKS":
					sql = "SELECT distinct " + fields + " FROM tasks as mt, readers_tasks as rt where mt.docid = " + key + " and exists "
					        + "(select * from readers_tasks where readers_tasks.docid = rt.docid and readers_tasks.username in (" + cuID + "))"
					        + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				case "EXECUTIONS":
					sql = "SELECT distinct " + fields + " FROM executions as mt, readers_executions as re where mt.docid = " + key + " and exists "
					        + "(select * from readers_executions where readers_executions.docid = re.docid and readers_executions.username in ("
					        + cuID + "))" + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				case "PROJECTS":
					sql = "SELECT distinct " + fields + " FROM projects as mt, readers_projects as rp where mt.docid = " + key + " and exists "
					        + "(select * from readers_projects where readers_projects.docid = rp.docid and readers_projects.username in (" + cuID
					        + "))" + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				case "TOPICS":
					sql = "SELECT distinct " + fields + " FROM topics as mt, readers_topics as rp where mt.docid = " + key + " and exists "
					        + "(select * from readers_topics where readers_topics.docid = rp.docid and readers_topics.username in (" + cuID + "))"
					        + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				case "POSTS":
					sql = "SELECT distinct " + fields + " FROM posts as mt, readers_posts as rp where mt.docid = " + key + " and exists "
					        + "(select * from readers_posts where readers_posts.docid = rp.docid and readers_posts.username in (" + cuID + "))"
					        + (filterCondition.length() != 0 ? " and " + filterCondition : "") + getOrderCondition(sorting) + " "
					        + getPagingCondition(pageSize, pageNum) + ";";
					break;
				default:
					break;
				}
				PreparedStatement pst_doc = conn.prepareStatement(sql);
				ResultSet rs_doc = pst_doc.executeQuery();
				while (rs_doc.next()) {
					ViewEntry entry = new ViewEntry(db, rs_doc, new HashSet<DocID>(), user);
					coll.add(entry);
				}
				rs_doc.close();
				pst_doc.close();
			}

			conn.commit();
			pst.close();
			rs.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);

		}
		if (pageNum == 0) {
			pageNum = RuntimeObjUtil.countMaxPage(coll.getCount(), pageSize);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

}
