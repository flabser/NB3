package kz.flabs.dataengine.mssql.queryformula;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.Filter;

import java.util.HashMap;
import java.util.Set;

public class TaskQueryFormula extends QueryFormula{
	
	public TaskQueryFormula(String queryID, FormulaBlocks pb) {
		super(queryID, pb);
	}
	
	public String getSQL(Set<String> complexUserID) {		
		String sql = "SELECT DISTINCT ";
		String fromSQLpart = " TASKS t, READERS_TASKS rt";
		String fieldsSQLpart = "t.DDBID, t.DOCID, t.DOCTYPE, t.HAS_ATTACHMENT, t.FORM, t.DBD, t.VIEWTEXT, t.TASKVN, " + DatabaseUtil.getViewTextList("t") + ", t.VIEWNUMBER, t.VIEWDATE, " +
				"TASKAUTHOR, TASKVN, TASKDATE, CONTENT, BRIEFCONTENT, COMMENT, TASKTYPE, HAR, PROJECT, DEFAULTRULEID, CONTROLTYPE, CTRLDATE, CYCLECONTROL, ALLCONTROL, ISOLD";		
		
		String whereSQLpart = " WHERE t.docid = rt.docid and rt.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ") AND ";
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				whereSQLpart += key + " = " + conditions.get(key) + " and ";
			}
		}
		
		whereSQLpart = getConditions(whereSQLpart);
		sql += fieldsSQLpart + " FROM " + fromSQLpart;
		sql += whereSQLpart;		
		
		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		}

		if (preparedBlocks.isSortBy){
			SortByBlock sortBlock = preparedBlocks.getSortByBlock();
			sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);

		}
		
		return sql;	

	}
	
	public String getSQLCount(Set<String> complexUserID) {
		
		String sql = "SELECT "; 
		String fromSQLpart = " TASKS t, READERS_TASKS rt";
		String fieldsSQLpart = "count (DISTINCT t.DOCID) ";		
		String whereSQLpart = " WHERE t.docid = rt.docid and rt.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ") AND ";
		
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				whereSQLpart += key + " = " + conditions.get(key) + " and ";
			}
		}
		
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
