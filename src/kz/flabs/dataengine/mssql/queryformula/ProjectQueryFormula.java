package kz.flabs.dataengine.mssql.queryformula;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;

import java.util.HashMap;
import java.util.Set;

public class ProjectQueryFormula extends QueryFormula{

	public ProjectQueryFormula(String queryID, FormulaBlocks pb) {
		super(queryID, pb);
	}
	
	public String getSQL(Set<String> complexUserID, int limit, int offset) {		
		String fromSQLpart = " PROJECTS p ";
		String fieldsSQLpart = "DOCID, DOCTYPE, HAS_ATTACHMENT, FORM, VIEWTEXT, " + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, VN, VNNUMBER, DOCVERSION, ISREJECTED, COORDSTATUS, TOPICID, PROJECT, DDBID, RESPONSIBLE ";
		  
		String sql = "SELECT DISTINCT ";		
		String whereSQLpart = " WHERE ";
		String intersectSQLpart = " p.docid in (select docid from readers_projects where username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				if (!key.equalsIgnoreCase("responsiblesection")) {
					String value = (key.equalsIgnoreCase("author") ? "'" + conditions.get(key) + "'" : conditions.get(key));
					whereSQLpart += key + " = " + value + " and ";
				} else {
					intersectSQLpart += " intersect select docid from coordblocks where id in (select blockid from coordinators where coordtype = " + ICoordConst.COORDINATOR_TYPE_SIGNER + " and coordinator = '" + conditions.get(key) + "')"; 
				}
			}
		}
		intersectSQLpart += ")";
		whereSQLpart += intersectSQLpart + " and ";
		whereSQLpart = getConditions(whereSQLpart);
		sql += fieldsSQLpart + " FROM " + fromSQLpart;
		sql += whereSQLpart;		

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

	public String getSQLCount(Set<String> complexUserID) {

		String sql = "SELECT "; 
		String fromSQLpart = " PROJECTS p, READERS_PROJECTS rp";
		String fieldsSQLpart = "count (DISTINCT p.DOCID) ";		
		String whereSQLpart = " WHERE ";
		String intersectSQLpart = " p.docid in (select docid from readers_projects where username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				if (!key.equalsIgnoreCase("responsiblesection")) {
					String value = (key.equalsIgnoreCase("author") ? "'" + conditions.get(key) + "'" : conditions.get(key));
					whereSQLpart += key + " = " + value + " and ";
				} else {
					intersectSQLpart += " intersect select docid from coordblocks where id in (select blockid from coordinators where coordtype = " + ICoordConst.COORDINATOR_TYPE_SIGNER + " and coordinator = '" + conditions.get(key) + "')"; 
				}
			}
		}
		intersectSQLpart += ")";
		whereSQLpart += intersectSQLpart + " and ";
		whereSQLpart = getConditions(whereSQLpart);
		sql += fieldsSQLpart + " FROM " + fromSQLpart;
		sql += whereSQLpart;		

		return sql;	

	}

	protected String getConditions(String whereSQLpart){
		String brackets = "";
		for (Block part : preparedBlocks.blocks) {				
			whereSQLpart += part.getContent();							
		}	
		return whereSQLpart + brackets;
	}


}
