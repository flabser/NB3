package kz.flabs.dataengine.h2.glossary;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.h2.queryformula.QueryFormula;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;

import java.util.Set;

public class GlossaryQueryFormula extends QueryFormula {

	public GlossaryQueryFormula(String queryID, FormulaBlocks b) {
		super(queryID, b);	
		table = "GLOSSARY";
		customFieldsTable  = "CUSTOM_FIELDS_GLOSSARY";
	}

	public String getGroupCondition(String value){		
		/*String whereSQLpart = " WHERE GLOSSARY.DOCID IN (select GLOSSARY.docid from GLOSSARY, CUSTOM_FIELDS_GLOSSARY "
                + "WHERE GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID AND (" + preparedBlocks.getGroupByBlock().getGroup() + " AND " + preparedBlocks.getGroupByBlock().getCondition(value) + ")) AND "; 		  		
 		whereSQLpart = getConditions(whereSQLpart);	*/
		String whereSQLpart = "where docid in (select descendant from glossary_tree_path where ancestor = " + value + " and length = 1) ";
		return whereSQLpart;		
	}
	
	public String getSQL() {
		String sql = "SELECT ";
		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sql;
		}else{
			String fieldsSQLpart = " glossary.author, glossary.regdate, glossary.parentdocid, glossary.parentdoctype, glossary.defaultruleid, glossary.docid, glossary.lastupdate, glossary.doctype, glossary.viewtext, " + DatabaseUtil.getViewTextList("glossary") + ", glossary.viewnumber, glossary.viewdate, glossary.form, glossary.ddbid ", fromSQLpart = "GLOSSARY ", whereSQLpart = " WHERE ",
					havingSQLpart = " HAVING ";

			if (preparedBlocks.isGroupBy){
				sql = "select ancestor, count(descendant) from glossary_tree_path where ancestor in (select docid from glossary where form='" + preparedBlocks.getGroupByBlock().fieldName + "') and length = 1 group by ancestor";
				return sql;
			} 

			whereSQLpart = getConditions(whereSQLpart);	
			havingSQLpart = getConditions(havingSQLpart);
			sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

			if (preparedBlocks.isGroupBy){
				sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
			} else {			   
				if (preparedBlocks.isSortBy){
					SortByBlock sortBlock = preparedBlocks.getSortByBlock();
					if (sortBlock.fieldName.equalsIgnoreCase("frequency_use")){
						sql = "select " + fieldsSQLpart + " from " + fromSQLpart + "left join custom_fields as cf on cf.valueasglossary = glossary.docid " +
								" group by glossary.docid, glossary.viewtext " +
								havingSQLpart +
								" order by count(cf.valueasglossary) " + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER); 
					} else {
						sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
					} 
				}
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

			/*	if (preparedBlocks.isGroupBy){
				fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
				fromSQLpart += "," + "CUSTOM_FIELDS_GLOSSARY";
				if (!whereSQLpart.equals(""))whereSQLpart += " AND ";
				whereSQLpart += " " + "GLOSSARY.DOCID = " + "CUSTOM_FIELDS_GLOSSARY.DOCID and " + preparedBlocks.getGroupByBlock().groupByCondition + "AND";			
			}*/

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
	public String getSQL(Set<String> complexUserID, int limit, int offset) {
		String sql = "SELECT ";
		if (preparedBlocks.isDirectQuery){
			sql = preparedBlocks.sql;
		}else{
			String fieldsSQLpart = "*", fromSQLpart = "GLOSSARY", whereSQLpart = " WHERE ";

			/*	if (preparedBlocks.isGroupBy){
				fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
				fromSQLpart += "," + "CUSTOM_FIELDS_GLOSSARY";
				if (!whereSQLpart.equals(""))whereSQLpart += " AND ";
				whereSQLpart += " " + "GLOSSARY.DOCID = " + "CUSTOM_FIELDS_GLOSSARY.DOCID and " + preparedBlocks.getGroupByBlock().groupByCondition + "AND";			
			}*/

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

}
