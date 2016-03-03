package kz.flabs.dataengine.postgresql.filters;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFilters;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.util.Util;

public class Filters implements IFilters {
	private IDatabase db;
	private IDBConnectionPool dbPool;

	public Filters(IDatabase db) {
		this.db = db;
		// this.dbPool = db.getConnectionPool();
	}

	public String getSQLQueryFromFilter(Filter filter, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize, boolean count) {
		HashMap<String, String> conditions = filter.getConditions();
		StringBuffer sql = new StringBuffer(1000);
		String tableName = "";
		String doctype = conditions.get("doctype");
		String formSQL = "";
		if (doctype.equalsIgnoreCase("")) {
			tableName = "all";
		} else if (doctype.equalsIgnoreCase("in") || doctype.equalsIgnoreCase("ish") || doctype.equalsIgnoreCase("l")
		        || doctype.equalsIgnoreCase("sz")) {
			tableName = "maindocs";
			formSQL = tableName + ".form = '" + doctype + "'";
		} else if (doctype.equalsIgnoreCase("task")) {
			tableName = "tasks";
		} else if (doctype.equalsIgnoreCase("outprj")) {
			tableName = "projects";
			formSQL = tableName + ".form = 'outdocprj'";
		} else if (doctype.equalsIgnoreCase("workprj")) {
			tableName = "projects";
			formSQL = tableName + ".form = 'workdocprj'";
		}
		formSQL = formSQL.toUpperCase();

		String keywordSQL = "";
		String keywordAddSQL = "";
		if (!conditions.get("keyword").equalsIgnoreCase("")) {
			keywordSQL = " plainto_tsquery('" + conditions.get("keyword") + "') q ";
			if (!tableName.equalsIgnoreCase("maindocs")) {
				keywordAddSQL = tableName + "." + "fts @@ q ";
			} else {
				keywordAddSQL = "cf.fts @@ q ";
			}
		}
		String projectSQL = "";
		String subQuery = "select docid from custom_fields as cf  ";
		if (!conditions.get("project").equalsIgnoreCase("")) {
			if (tableName.equalsIgnoreCase("maindocs")) {
				projectSQL = subQuery + " where (cf.name = 'project' and cf.valueasglossary = " + conditions.get("project") + ") ";
			} else if (tableName.equalsIgnoreCase("tasks") || tableName.equalsIgnoreCase("projects")) {
				projectSQL = tableName + ".project = " + conditions.get("project");
			}
		}
		String authorSQL = "";
		if (!conditions.get("author").equalsIgnoreCase("")) {
			if (tableName.equalsIgnoreCase("maindocs")) {
				authorSQL = subQuery + " where (cf.name = 'author' and cf.value = '" + conditions.get("author") + "') ";
			} else if (tableName.equalsIgnoreCase("tasks") || tableName.equalsIgnoreCase("projects")) {
				authorSQL = tableName + ".author = '" + conditions.get("author") + "'";
			}
		}
		String categorySQL = "";
		if (!conditions.get("category").equalsIgnoreCase("")) {
			if (tableName.equalsIgnoreCase("maindocs")) {
				categorySQL = subQuery + " where (cf.name = 'category' and cf.valueasglossary = " + conditions.get("category") + ") ";
			} else if (tableName.equalsIgnoreCase("tasks") || tableName.equalsIgnoreCase("projects")) {
				categorySQL = tableName + ".category = " + conditions.get("category");
			}
		}
		String dateSQL = "";

		if (!conditions.get("datefrom").equalsIgnoreCase("") && !conditions.get("dateto").equalsIgnoreCase("")) {
			try {
				Date dateFrom = Util.simpleDateFormat.parse(conditions.get("datefrom"));
				Date dateTo = Util.simpleDateFormat.parse(conditions.get("dateto"));
				dateSQL = tableName + ".regdate between '" + Util.simpleDateTimeFormat.format(dateFrom) + "' and '"
				        + Util.simpleDateTimeFormat.format(dateTo) + "'";
			} catch (ParseException e) {
				// DatabaseUtil.errorPrint(db.getDbID(), e);
			}
		}
		boolean needAnd = false;
		boolean needIntersect = false;
		String readerSQL = " select docid from " + "readers_" + tableName + " where " + "readers_" + tableName + ".username = '" + absoluteUserID
		        + "'";
		if (!tableName.equalsIgnoreCase("maindocs")) {
			readerSQL = tableName + ".docid in (" + readerSQL + ")";
		}
		if (count) {
			sql.append("select count(*) from " + tableName);
		} else {
			sql.append("select * from " + tableName);
		}
		if (tableName.equalsIgnoreCase("maindocs")) {
			sql.append(" where " + tableName + ".docid in (");
			if (!projectSQL.equalsIgnoreCase("")) {
				if (needIntersect) {
					sql.append(" intersect ");
				}
				sql.append(projectSQL);
				needIntersect = true;
			}
			if (!authorSQL.equalsIgnoreCase("")) {
				if (needIntersect) {
					sql.append(" intersect ");
				}
				sql.append(authorSQL);
				needIntersect = true;
			}
			if (!categorySQL.equalsIgnoreCase("")) {
				if (needIntersect) {
					sql.append(" intersect ");
				}
				sql.append(categorySQL);
				needIntersect = true;
			}
			if (needIntersect) {
				sql.append(" intersect ");
			}
			sql.append(readerSQL);
			needIntersect = true;
			if (!keywordSQL.equalsIgnoreCase("")) {
				if (needIntersect) {
					sql.append(" intersect ");
				}
				sql.append(subQuery + " , " + keywordSQL + " where " + keywordAddSQL);
			}
			sql.append(") ");
			needAnd = true;
			if (!dateSQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(dateSQL);
				needAnd = true;
			}
			if (!formSQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(formSQL);
				needAnd = true;
			}
		} else {
			if (!keywordSQL.equalsIgnoreCase("")) {
				sql.append(" , " + keywordSQL);
			}
			sql.append(" where ");
			if (!keywordSQL.equalsIgnoreCase("")) {
				sql.append(keywordAddSQL);
				needAnd = true;
			}
			if (!projectSQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(projectSQL);
				needAnd = true;
			}
			if (!formSQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(formSQL);
				needAnd = true;
			}
			if (!authorSQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(authorSQL);
				needAnd = true;
			}
			if (!categorySQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(categorySQL);
				needAnd = true;
			}
			if (!dateSQL.equalsIgnoreCase("")) {
				if (needAnd) {
					sql.append(" and ");
				}
				sql.append(dateSQL);
				needAnd = true;
			}
			if (needAnd) {
				sql.append(" and ");
			}
			sql.append(readerSQL);
		}
		if (!count) {
			sql.append(" LIMIT " + pageSize + " OFFSET " + offset);
		}
		return sql.toString();
	}

}