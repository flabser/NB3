package kz.flabs.dataengine;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Set;

import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.webrule.constants.QueryType;
import kz.lof.server.Server;

public class DatabaseUtil implements Const {

	public static void errorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLException sqle = (SQLException) e;
			SQLExceptionPrint(sqle);
		} else {
			Server.logger.errorLogEntry(e.toString());
			e.printStackTrace();
		}
	}

	public static void debugErrorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLException sqle = (SQLException) e;
			SQLExceptionPrintDebug(sqle);
		} else {
			Server.logger.errorLogEntry(e.toString());
			e.printStackTrace();
		}
	}

	public static String getViewTextValues(BaseDocument doc) {
		String viewTextList = "";
		int fieldSize = 0;

		if (viewTextList.endsWith(",")) {
			viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
		}
		return viewTextList;
	}

	public static void errorPrint(String DbID, Throwable e) {
		Server.logger.errorLogEntry(DbID);
		if (e instanceof SQLException) {
			SQLException sqle = (SQLException) e;
			SQLExceptionPrintDebug(sqle);
		} else {
			Server.logger.errorLogEntry(e.toString());
			e.printStackTrace();
		}
	}

	public static void errorPrint(Throwable e, String sql) {
		Server.logger.errorLogEntry(sql);
		if (e instanceof SQLException) {
			SQLException sqle = (SQLException) e;
			SQLExceptionPrintDebug(sqle);
		} else {
			Server.logger.errorLogEntry(e.toString());
			e.printStackTrace();
		}
	}

	public static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			Server.logger.errorLogEntry("SQLState:   " + sqle.getSQLState());
			Server.logger.errorLogEntry("Severity: " + sqle.getErrorCode());
			Server.logger.errorLogEntry("Message:  " + sqle.getMessage());
			// Server.logger.errorLogEntry(sqle);
			sqle = sqle.getNextException();
		}
	}

	public static void SQLExceptionPrintDebug(SQLException sqle) {
		while (sqle != null) {
			Server.logger.errorLogEntry("SQLState:   " + sqle.getSQLState());
			Server.logger.errorLogEntry("Severity: " + sqle.getErrorCode());
			Server.logger.errorLogEntry("Message:  " + sqle.getMessage());
			Server.logger.errorLogEntry(sqle);
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}

	public static boolean hasTable(String tableName, Connection conn) throws SQLException {
		try {
			DatabaseMetaData metaData = null;
			metaData = conn.getMetaData();
			String[] tables = { "TABLE" };
			ResultSet rs = metaData.getTables(null, null, null, tables);
			while (rs.next()) {
				String table = rs.getString("TABLE_NAME");
				if (tableName.equalsIgnoreCase(table)) {
					return true;
				}
			}
			return false;
		} catch (Throwable e) {
			return false;
		}
	}

	public static boolean hasView(String viewName, Connection conn) throws SQLException {
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM INFORMATION_SCHEMA.VIEWS where upper(table_name) = '" + viewName.toUpperCase() + "'");
			if (rs.next()) {
				return true;
			}
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;

		}
	}

	public static boolean hasMaterializedView(String viewName, Connection conn) throws SQLException {
		try {
			String sql = "SELECT (current_database())::information_schema.sql_identifier AS table_catalog, "
			        + "(nc.nspname)::information_schema.sql_identifier AS table_schema,(c.relname)::information_schema.sql_identifier AS table_name,"
			        + "(CASE WHEN pg_has_role(c.relowner, 'USAGE'::text) THEN pg_get_viewdef(c.oid) ELSE NULL::text END)::"
			        + "information_schema.character_data AS view_definition,(CASE WHEN ('check_option=cascaded'::text = ANY (c.reloptions)) THEN "
			        + "'CASCADED'::text WHEN ('check_option=local'::text = ANY (c.reloptions)) THEN 'LOCAL'::text ELSE 'NONE'::text END)"
			        + "::information_schema.character_data AS check_option,(CASE WHEN ((pg_relation_is_updatable((c.oid)::regclass, false)"
			        + " & 20) = 20) THEN 'YES'::text ELSE 'NO'::text END)::information_schema.yes_or_no AS is_updatable,(CASE WHEN "
			        + "((pg_relation_is_updatable((c.oid)::regclass, false) & 8) = 8) THEN 'YES'::text ELSE 'NO'::text END)"
			        + "::information_schema.yes_or_no AS is_insertable_into,(CASE WHEN (EXISTS ( SELECT 1 FROM pg_trigger WHERE "
			        + "((pg_trigger.tgrelid = c.oid) AND (((pg_trigger.tgtype)::integer & 81) = 81)))) THEN 'YES'::text ELSE 'NO'"
			        + "::text END)::information_schema.yes_or_no AS is_trigger_updatable,( CASE WHEN (EXISTS ( SELECT 1 FROM pg_trigger"
			        + " WHERE ((pg_trigger.tgrelid = c.oid) AND (((pg_trigger.tgtype)::integer & 73) = 73)))) THEN 'YES'::text ELSE 'NO'"
			        + "::text END)::information_schema.yes_or_no AS is_trigger_deletable,(CASE WHEN (EXISTS ( SELECT 1 FROM pg_trigger"
			        + " WHERE ((pg_trigger.tgrelid = c.oid) AND (((pg_trigger.tgtype)::integer & 69) = 69)))) THEN 'YES'::text ELSE 'NO'"
			        + "::text END)::information_schema.yes_or_no AS is_trigger_insertable_into  FROM pg_namespace nc,  pg_class c WHERE "
			        + "((((c.relnamespace = nc.oid) AND (c.relkind = 'm'::\"char\")) AND (NOT pg_is_other_temp_schema(nc.oid))) AND "
			        + "((pg_has_role(c.relowner, 'USAGE':: text) OR has_table_privilege(c.oid, 'SELECT, INSERT, UPDATE, DELETE, TRUNCATE,"
			        + " REFERENCES, TRIGGER'::text)) OR has_any_column_privilege(c.oid, 'SELECT, INSERT, UPDATE, REFERENCES'::text)));";

			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				return true;
			}
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;

		}
	}

	public static boolean hasProcedureAndTriger(String name, Connection conn) throws SQLException {
		try {
			DatabaseMetaData metaData = null;
			metaData = conn.getMetaData();
			ResultSet rs = metaData.getProcedures(null, null, null);
			while (rs.next()) {
				String procedure = rs.getString("PROCEDURE_NAME");
				if (name.equalsIgnoreCase(procedure)) {
					return true;
				}
			}
			return false;
		} catch (Throwable e) {
			return false;
		}
	}

	public static boolean hasTrigger(String name, Connection conn) throws SQLException {
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from sys.triggers where name = '" + name + "'");
			if (rs.next()) {
				return true;
			}
			return false;
		} catch (Throwable e) {
			return false;
		}
	}

	public static String prepareListToQuery(Collection<String> elements) {
		StringBuffer result = new StringBuffer(1000);
		if (elements != null) {
			for (String element : elements) {
				result.append("'" + element + "',");
			}
			if (result.length() != 0) {
				result = result.deleteCharAt(result.length() - 1);
			}
		}
		return result.toString();
	}

	public static String getPrimaryKeyColumnName(int docType) {
		String columnName;
		switch (docType) {
		case DOCTYPE_ORGANIZATION:
			columnName = "ORGID";
			break;
		case DOCTYPE_DEPARTMENT:
			columnName = "DEPID";
			break;
		case DOCTYPE_EMPLOYER:
			columnName = "EMPID";
			break;
		case DOCTYPE_GROUP:
			columnName = "GROUPID";
			break;
		case DOCTYPE_ACTIVITY_ENTRY:
		case DOCTYPE_RECYCLE_BIN_ENTRY:
			columnName = "ID";
			break;
		default:
			columnName = "DOCID";
		}
		return columnName;
	}

	public static String getViewTextList(String prefix) {
		String list = "";

		if (list.endsWith(",")) {
			list = list.substring(0, list.length() - 1);
		}
		return list;
	}

	/*
	 * public static String get() { String viewTextList = ""; for (int i = 1; i
	 * <= DatabaseConst.VIEWTEXT_COUNT; i++) { viewTextList += "'" +
	 * doc.getViewTextList().get(i).replaceAll("'", "''") + "',"; } if
	 * (viewTextList.endsWith(",")) { viewTextList = viewTextList.substring(0,
	 * viewTextList.length()-1); } return viewTextList; }
	 */

	public static String getCustomBlobsTableName(int docType) {
		String tableName;
		switch (docType) {
		case DOCTYPE_MAIN:
		case DOCTYPE_TASK:
		case DOCTYPE_EXECUTION:
		case DOCTYPE_PROJECT:
		case DOCTYPE_EMPLOYER:
			tableName = "CUSTOM_BLOBS_" + getMainTableName(docType);
			break;
		default:
			throw new IllegalArgumentException("Document type is unknown");
		}

		return tableName;
	}

	@Deprecated
	public static String getReadersTableName(int docType) {
		return "READERS_" + getMainTableName(docType);
	}

	@Deprecated
	public static String getMainTableName(int docType) {
		String tableName = "";
		switch (docType) {
		case DOCTYPE_MAIN:
			tableName = "MAINDOCS";
			break;
		case DOCTYPE_TASK:
			tableName = "TASKS";
			break;
		case DOCTYPE_EXECUTION:
			tableName = "EXECUTIONS";
			break;
		case DOCTYPE_PROJECT:
			tableName = "PROJECTS";
			break;
		case DOCTYPE_GLOSSARY:
			tableName = "GLOSSARY";
			break;
		case DOCTYPE_ORGANIZATION:
			tableName = "ORGANIZATIONS";
			break;
		case DOCTYPE_DEPARTMENT:
			tableName = "DEPARTMENTS";
			break;
		case DOCTYPE_EMPLOYER:
			tableName = "EMPLOYERS";
			break;
		case DOCTYPE_GROUP:
			tableName = "GROUPS";
			break;
		case DOCTYPE_ACTIVITY_ENTRY:
			tableName = "USERS_ACTIVITY";
			break;
		case DOCTYPE_RECYCLE_BIN_ENTRY:
			tableName = "RECYCLE_BIN";
			break;
		case DOCTYPE_TOPIC:
			tableName = "TOPICS";
			break;
		case DOCTYPE_POST:
			tableName = "POSTS";
			break;
		default:
			throw new IllegalArgumentException("Document type is unknown");
		}
		return tableName;
	}

	public static boolean hasAbsoluteReadAccess(Set<String> complexUserID) {
		return complexUserID.contains(Const.observerGroup[0]) && complexUserID.contains(Const.supervisorGroup[0]);
	}

	public static boolean hasFTIndex(Connection conn, String tableName) {
		String sql = "SELECT COUNT(*) FROM sys.fulltext_indexes where object_id = object_id('" + tableName + "')";
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count > 0) {
					return true;
				} else {
					return false;
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(e, sql);
		}
		return false;
	}

	public static boolean hasFTCatalog(Connection conn, String catalogName) {
		String sql = "SELECT COUNT(*) FROM sys.fulltext_catalogs where name = '" + catalogName + "'";
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count > 0) {
					return true;
				} else {
					return false;
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(e, sql);
		}
		return false;
	}

	public static String getCustomTableName(QueryType docType) {
		if (docType == QueryType.GLOSSARY) {
			return "CUSTOM_FIELDS_GLOSSARY";
		} else {
			return "CUSTOM_FIELDS";
		}
	}

	@Deprecated
	public static String resolveElement(int docType) {
		switch (docType) {
		case Const.DOCTYPE_TASK:
			return "task";
		case Const.DOCTYPE_EXECUTION:
			return "execution";
		case Const.DOCTYPE_PROJECT:
			return "project";
		default:
			return "document";
		}
	}
}
