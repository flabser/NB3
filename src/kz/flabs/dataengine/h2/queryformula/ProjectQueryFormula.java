package kz.flabs.dataengine.h2.queryformula;

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
	
	public String getSQL(Set<String> complexUserID) {		
	/*	String sql = "SELECT DISTINCT ";
		String fromSQLpart = " PROJECTS p, READERS_PROJECTS rp";
		String fieldsSQLpart = "p.DOCID, p.DOCTYPE, p.HAS_ATTACHMENT, p.FORM, p.VIEWTEXT, p.VN, p.VNNUMBER, p.DOCVERSION, p.ISREJECTED, p.VIEWTEXT1, p.VIEWTEXT2, p.VIEWTEXT3, p.VIEWNUMBER, p.VIEWDATE, p.COORDSTATUS ";		
		
		String whereSQLpart = " WHERE p.docid = rp.docid and rp.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ") AND ";
		
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				String value = (key.equalsIgnoreCase("author") || key.equalsIgnoreCase("responsiblesection") ? "'" + conditions.get(key) + "'" : conditions.get(key));
				whereSQLpart += key + " = " + value + " and ";
			}
		}*/
		
		String sql = "SELECT DISTINCT ";
		String fromSQLpart = " PROJECTS p ";
		String fieldsSQLpart = "p.DDBID, p.DOCID, p.DOCTYPE, p.HAS_ATTACHMENT, p.FORM, p.VIEWTEXT, p.VN, p.VNNUMBER, p.DOCVERSION, p.ISREJECTED, " + DatabaseUtil.getViewTextList("p") + ", p.VIEWNUMBER, p.VIEWDATE, p.COORDSTATUS, p.TOPICID, p.PROJECT, p.RESPONSIBLE, p.CTRLDATE ";
		
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
		whereSQLpart += intersectSQLpart;
		if (!this.preparedBlocks.blocks.isEmpty()) {
			whereSQLpart += " and ";
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
