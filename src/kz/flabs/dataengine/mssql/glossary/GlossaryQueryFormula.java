package kz.flabs.dataengine.mssql.glossary;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;

import java.util.Set;

public class GlossaryQueryFormula extends kz.flabs.dataengine.h2.glossary.GlossaryQueryFormula{

	public GlossaryQueryFormula(String queryID, FormulaBlocks b) {
		super(queryID, b);			
	}

	public String getGroupCondition(String value){		
		String whereSQLpart = "where docid in (select descendant from glossary_tree_path where ancestor = " + value + " and length = 1) ";
		return whereSQLpart;		
	}

	public String getSQL() {
		String sql = "SELECT ";
		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sql;
		}else{
			String fieldsSQLpart = " author, regdate, parentdocid, parentdoctype, defaultruleid, docid, ddbid, lastupdate, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate, form ", fromSQLpart = "GLOSSARY ", whereSQLpart = " WHERE ",
					havingSQLpart = " HAVING ";

			if (preparedBlocks.isGroupBy){
				sql = "select ancestor, count(descendant) from glossary_tree_path where ancestor in (select docid from glossary where form='" + preparedBlocks.getGroupByBlock().fieldName + "') and length = 1 group by ancestor";
				return sql;
			} 

			whereSQLpart = getConditions(whereSQLpart);	
			havingSQLpart = getConditions(havingSQLpart);
			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

			String groupSQLpart = "";
			if (preparedBlocks.isGroupBy){
				groupSQLpart = " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
				sql += groupSQLpart;
			}

			SortByBlock sortBlock = null;
			if (preparedBlocks.isSortBy){
				sortBlock = preparedBlocks.getSortByBlock();
				String orderSQLpart = " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
				sql += orderSQLpart;

			}
		}
		return sql;
	}

	public String getSQL(Set<String> complexUserID, int limit, int offset) {
		String sql = "SELECT ";
		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sql;
		}else{
			String fieldsSQLpart = " author, regdate, parentdocid, parentdoctype, defaultruleid, docid, ddbid, lastupdate, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate, form ", fromSQLpart = "GLOSSARY ", whereSQLpart = " WHERE ",
					havingSQLpart = " HAVING ";

			if (preparedBlocks.isGroupBy){
				sql = "select ancestor, count(descendant) from glossary_tree_path where ancestor in (select docid from glossary where form='" + preparedBlocks.getGroupByBlock().fieldName + "') and length = 1 group by ancestor";
				return sql;
			} 

			whereSQLpart = getConditions(whereSQLpart);	
			havingSQLpart = getConditions(havingSQLpart);
			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

			String groupSQLpart = "";
			if (preparedBlocks.isGroupBy){
				groupSQLpart = " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
				sql += groupSQLpart;
			}

			SortByBlock sortBlock = null;
			if (preparedBlocks.isSortBy){
				sortBlock = preparedBlocks.getSortByBlock();
				String orderSQLpart = " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
				sql += orderSQLpart;

			}

			String sqlWithPaging = " DECLARE @intStartRow int; " +
					" DECLARE @intEndRow int; " +
					" DECLARE @intPage int = " + offset + ";" + 
					" DECLARE @intPageSize int = " + limit + ";" + 
					" SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
					" SET @intEndRow = @intPage * @intPageSize;" +
					" WITH blogs AS" +
					" (SELECT " + fieldsSQLpart + ", " +
					" ROW_NUMBER() OVER(ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER) + ") as intRow, " +
					" COUNT(" + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + ") OVER() AS intTotalHits " +
					" FROM " + fromSQLpart + " " + whereSQLpart + " ) " +
					" SELECT " + fieldsSQLpart + " FROM blogs" +
					" WHERE intRow BETWEEN @intStartRow AND @intEndRow" + groupSQLpart;		

			if (limit == -1 && --offset == 0) {
				return sql;	
			} else {
				return sqlWithPaging;
			}


		}
		return sql;
	}

	public String getSQL(Set<String> complexUserID) {
		String sql = "SELECT ";
		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sql;
		}else{
			String fieldsSQLpart = "*", fromSQLpart = "GLOSSARY", whereSQLpart = " WHERE ";
			if (preparedBlocks.isGroupBy){
				fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
				fromSQLpart += ", CUSTOM_FIELDS_GLOSSARY";			
				whereSQLpart += " GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " ANd ";
			}			

			whereSQLpart = getConditions(whereSQLpart);	
			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

			if (preparedBlocks.isGroupBy){
				sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
			} else {
				if(preparedBlocks.isSortBy){
					if (preparedBlocks.isSortBy){
						SortByBlock sortBlock = preparedBlocks.getSortByBlock();
						sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
					}
				}
			}
		}
		return sql;
	}

	public String getSQLGroupCount() {
		String sql = "select ancestor, count(descendant) from glossary_tree_path where ancestor in (select docid from glossary where form='" + preparedBlocks.getGroupByBlock().fieldName + "') and length = 1 group by ancestor";
		sql = "SELECT COUNT(*) FROM (" + sql + ") groups";
		return sql;
	}

	public String getSQLGroupCount(Set<String> complexUserID) {
		String sql = "SELECT ";
		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sql;
		}else{
			String fieldsSQLpart = "*", fromSQLpart = "GLOSSARY", whereSQLpart = " WHERE ";

			if (preparedBlocks.isGroupBy){
				fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
				fromSQLpart += ", CUSTOM_FIELDS_GLOSSARY";			
				whereSQLpart += " GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " ANd ";
			}			

			whereSQLpart = getConditions(whereSQLpart);	
			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

			if (preparedBlocks.isGroupBy){
				sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
			} else {
				if(preparedBlocks.isSortBy){
					if (preparedBlocks.isSortBy){
						SortByBlock sortBlock = preparedBlocks.getSortByBlock();
						sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
					}
				}
			}
		}
		sql = "SELECT COUNT(*) FROM (" + sql + ") groups";
		return sql;
	}

	public String getSQLCount() {
		String fieldsSQLpart = " count(DISTINCT GLOSSARY.DOCID) ";
		String sql = "SELECT ", fromSQLpart = "GLOSSARY", whereSQLpart = " WHERE ";

		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sqlCount;
		}else{
			whereSQLpart = getConditions(whereSQLpart);		

			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;
		}

		return sql;
	}

	public String getSQLCount(Set<String> complexUserID) {
		String fieldsSQLpart = " count(DISTINCT GLOSSARY.DOCID) ";
		String sql = "SELECT ", fromSQLpart = "GLOSSARY", whereSQLpart = " WHERE ";

		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sqlCount;
		}else{
			whereSQLpart = getConditions(whereSQLpart);		

			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;
		}

		return sql;
	}

}


