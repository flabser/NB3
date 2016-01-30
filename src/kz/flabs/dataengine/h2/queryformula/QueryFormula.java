package kz.flabs.dataengine.h2.queryformula;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlockType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.Filter;

import java.util.HashMap;
import java.util.Set;

public class QueryFormula implements IQueryFormula {
	public String queryID;

	protected String table = "MAINDOCS";
	protected boolean hasFormCondition;	
	protected String formCondition = "";
	protected FormulaBlocks preparedBlocks;
	protected String customFieldsTable = "CUSTOM_FIELDS";

	private String categoryName;	

	public QueryFormula(String queryID, FormulaBlocks preparedBlocks){
		this.queryID = queryID;
		this.preparedBlocks = preparedBlocks;		
	}

	public String getQueryID(){
		return queryID;
	}

	public boolean isGroupBy() {
		if (preparedBlocks.isDirectQuery && preparedBlocks.sql.contains("group")){
			return true;
		}else{
			return preparedBlocks.isGroupBy;
		}
	}

	public SortByBlock getSortBlock(){
		return this.preparedBlocks.getSortByBlock();
	}

	@Override
	public Filter getQuickFilter() {
		return this.preparedBlocks.getQuickFilter();
	}
	
	@Override
	public void setQuickFilter(Filter filter) {
		this.preparedBlocks.setQuickFilter(filter);
	}
	
	@Override
	public FormulaBlocks getBlocks() {
		return preparedBlocks;
	}
	
	public boolean isSortBy(){
		if (preparedBlocks.isDirectQuery && preparedBlocks.sql.contains("order")){
			return true;
		}else{
			return preparedBlocks.isSortBy;
		}
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public String getSQLGroupCount(Set<String> complexUserID) {
		String fieldsSQLpart = " DISTINCT MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.DDBID, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM, MAINDOCS.REGDATE, " + DatabaseUtil.getViewTextList("MAINDOCS") + ", MAINDOCS.VIEWNUMBER, MAINDOCS.VIEWDATE ";
		String sql = "SELECT ", fromSQLpart = "MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID in (select docid from READERS_MAINDOCS where" +
						" MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND" +
						" READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";

		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				whereSQLpart += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
			}
		}
		whereSQLpart += ")";		
		
		if (preparedBlocks.isGroupBy){
			fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
			fromSQLpart += ", CUSTOM_FIELDS";			
			whereSQLpart += " And MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " ANd ";
		}else{
			whereSQLpart += " AND ";
		}

		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;	

		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		} else {
			if (preparedBlocks.isSortBy){
				SortByBlock sortBlock = preparedBlocks.getSortByBlock();
				sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
			}
		}

		sql = "SELECT COUNT(*) FROM (" + sql + ") groups";
		return sql;
	}

	public String getGroupCondition(String value){		
		String whereSQLpart = " AND MAINDOCS.DOCID IN (select MAINDOCS.docid from MAINDOCS, CUSTOM_FIELDS "
				+ "WHERE MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID AND (" + preparedBlocks.getGroupByBlock().getGroup() + " AND " + preparedBlocks.getGroupByBlock().getCondition(value) + ")) AND "; 		  		
		whereSQLpart = getConditions(whereSQLpart);		
		return whereSQLpart;		
	}

	public String getSQL(Set<String> complexUserID) {
		String fieldsSQLpart = " DISTINCT MAINDOCS.DDBID, MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.DDBID, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM, MAINDOCS.REGDATE, " + DatabaseUtil.getViewTextList("MAINDOCS") + ", MAINDOCS.VIEWNUMBER, MAINDOCS.VIEWDATE ";
		/*String sql = "SELECT ", fromSQLpart = "MAINDOCS, READERS_MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID=READERS_MAINDOCS.DOCID "
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/

		String sql = "SELECT ", fromSQLpart = "MAINDOCS",
			/*	whereSQLpart = " WHERE MAINDOCS.DOCID in (select docid from READERS_MAINDOCS where" +
						//" MAINDOCS.PARENTDOCTYPE = " + Const.DOCTYPE_UNKNOWN + " AND MAINDOCS.PARENTDOCID = 0 AND " + 
						" MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND" +
						" READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/
			whereSQLpart = " WHERE MAINDOCS.DOCID in (select docid from READERS_MAINDOCS where MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND" +
				" READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
		
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				whereSQLpart += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
			}
		}

		whereSQLpart += ")";



		if (preparedBlocks.isGroupBy){
			fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
			fromSQLpart += ", CUSTOM_FIELDS";			
			whereSQLpart += " And MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " ANd ";
		}else{
			whereSQLpart += " AnD ";
		}



		/*	String projectSQL = "";
		String subQuery = "select docid from custom_fields as cf  ";
		if (!conditions.get("project").equalsIgnoreCase("")) {
			projectSQL = subQuery + " where (cf.name = 'project' and cf.valueasglossary = " + conditions.get("project")+ ") ";		
		}

		String categorySQL = "";
		if (!conditions.get("category").equalsIgnoreCase("")) {
				categorySQL = subQuery + " where (cf.name = 'category' and cf.valueasglossary = " + conditions.get("category")+ ") ";
		}*/


		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;	

		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		} else {
			if (preparedBlocks.isSortBy){
				SortByBlock sortBlock = preparedBlocks.getSortByBlock();
				sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
			}
		}

		return sql;
	}

	public String getIDsSQL(Set<String> complexUserID) {
		String fieldsSQLpart = " DISTINCT MAINDOCS.DOCID ";
		String sql = "SELECT ", fromSQLpart = "MAINDOCS, READERS_MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID=READERS_MAINDOCS.DOCID "
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";

		if (preparedBlocks.isGroupBy){
			fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
			fromSQLpart += ", CUSTOM_FIELDS";           
			whereSQLpart += " And MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " ANd ";
		}else{
			whereSQLpart += " AnD ";
		}

		whereSQLpart = getConditions(whereSQLpart);     

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;   

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
		String fieldsSQLpart = " count(DISTINCT MAINDOCS.DOCID) ";
		/*String sql = "SELECT ", fromSQLpart = "MAINDOCS, READERS_MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID=READERS_MAINDOCS.DOCID "
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/

		String sql = "SELECT ", fromSQLpart = "MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID IN (select docid from READERS_MAINDOCS where" +
						//" MAINDOCS.PARENTDOCTYPE = " + Const.DOCTYPE_UNKNOWN + " AND MAINDOCS.PARENTDOCID = 0 AND " + 
						" MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND " +
						" READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";

		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				whereSQLpart += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
			}
		}
		whereSQLpart += ")";	
		
		whereSQLpart += " AND ";
		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

		return sql;
	}	

	protected String getConditions(String whereSQLpart){
		String brackets = "", where = "";
		for (Block part : preparedBlocks.blocks) {	
			if (part.blockType == FormulaBlockType.CONDITION){
				where += table + ".DOCID IN (select " + table + ".docid from " + table + ", " + customFieldsTable + " "
						+ "WHERE " + table + ".DOCID = " + customFieldsTable + ".DOCID aND " + part.getContent();
				brackets += ")";	
			}else{
				where += part.getContent();
			}				
		}	
		if (!where.trim().equalsIgnoreCase("")){
			return whereSQLpart += "(" + where + brackets + ")";
		}else{
			return "";
		}
	}

	@Override
	public String getSQLGroupCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQLCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL(Set<String> complexUserID, int pageSize, int offset) {
		String fieldsSQLpart = " DISTINCT MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.DDBID, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM, MAINDOCS.REGDATE, " + DatabaseUtil.getViewTextList("MAINDOCS") + ", MAINDOCS.VIEWNUMBER, MAINDOCS.VIEWDATE ";
		/*String sql = "SELECT ", fromSQLpart = "MAINDOCS, READERS_MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID=READERS_MAINDOCS.DOCID "
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/

		String sql = "SELECT ", fromSQLpart = "MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID in (select docid from READERS_MAINDOCS where" +
						//" MAINDOCS.PARENTDOCTYPE = " + Const.DOCTYPE_UNKNOWN + " AND MAINDOCS.PARENTDOCID = 0 AND " + 
						" MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND" +
						" READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
		
		Filter quickFilter = preparedBlocks.getQuickFilter();
		if (quickFilter != null && quickFilter.getEnable() == 1) {
			HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
			for (String key : conditions.keySet()) {
				whereSQLpart += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
			}
		}

		whereSQLpart += ")";



		if (preparedBlocks.isGroupBy){
			fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
			fromSQLpart += ", CUSTOM_FIELDS";			
			whereSQLpart += " And MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " ANd ";
		}else{
			whereSQLpart += " AnD ";
		}



		/*	String projectSQL = "";
		String subQuery = "select docid from custom_fields as cf  ";
		if (!conditions.get("project").equalsIgnoreCase("")) {
			projectSQL = subQuery + " where (cf.name = 'project' and cf.valueasglossary = " + conditions.get("project")+ ") ";		
		}

		String categorySQL = "";
		if (!conditions.get("category").equalsIgnoreCase("")) {
				categorySQL = subQuery + " where (cf.name = 'category' and cf.valueasglossary = " + conditions.get("category")+ ") ";
		}*/


		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;	

		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		} else {
			if (preparedBlocks.isSortBy){
				SortByBlock sortBlock = preparedBlocks.getSortByBlock();
				sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
			}
		}

        if (pageSize > 0) {
            sql += " LIMIT " + pageSize;
        }
        if (offset > 0) {
            sql += " OFFSET " + offset;
        }

		return sql;
	}

    @Override
    public String getSQL(Set<String> complexUserID, int pageSize, int offset, String[] filters, String[] sorting) {
        String fieldsSQLpart = " DISTINCT MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.DDBID, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM, MAINDOCS.REGDATE, " + DatabaseUtil.getViewTextList("MAINDOCS") + ", MAINDOCS.VIEWNUMBER, MAINDOCS.VIEWDATE ";

        String sql = "SELECT ", fromSQLpart = "MAINDOCS",
                whereSQLpart = " WHERE MAINDOCS.DOCID in (select docid from READERS_MAINDOCS where" +
                        " MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND" +
                        " READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";

        Filter quickFilter = preparedBlocks.getQuickFilter();
        if (quickFilter != null && quickFilter.getEnable() == 1) {
            HashMap<String, String> conditions = preparedBlocks.getQuickFilter().getConditions();
            for (String key : conditions.keySet()) {
                whereSQLpart += " intersect select docid from custom_fields as cf where (cf.name = '" + key + "' and cf.valueasglossary = " + conditions.get(key) + ") ";
            }
        }

        whereSQLpart += ")";

        if (preparedBlocks.isGroupBy) {
            fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
            fromSQLpart += ", CUSTOM_FIELDS";
            whereSQLpart += " AND MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " AND ";
        } else {
            whereSQLpart += " AND ";
        }

        whereSQLpart = getConditions(whereSQLpart);

        for (int i = 0; i < filters.length; i++) {
            String cond = filters[i];
            boolean and = false;
            if (cond != null && !"".equalsIgnoreCase(cond) && !"null".equalsIgnoreCase(cond)) {
                and = false;
                switch (i) {
                    case 0:
                        cond = "VIEWTEXT = '" + cond + "' ";
                        break;
                    case 1:
                    case 2:
                    case 3:
                        cond = "VIEWTEXT" + i + " = '" + cond + "' ";
                        break;
                    case 4:
                        cond = "VIEWNUMBER = " + cond + " ";
                        break;
                    case 5:
                        cond = "VIEWDATE = '" + cond + "' ";
                        break;
                    default:
                        cond = "VIEWTEXT = '" + cond + "'";
                        break;
                }
                and = true;
            }
            if (and) {
                whereSQLpart += " and " + cond;
            }
        }

        sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

        if (preparedBlocks.isGroupBy) {
            sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
        }

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
                tmpsql +=  cond + ",";
            }
        }

        if (!"".equalsIgnoreCase(tmpsql)) {
            sql += " ORDER BY " + tmpsql.substring(0, tmpsql.length()-1);
        }

        if (pageSize > 0) {
            sql += " LIMIT " + pageSize;
        }
        if (offset > 0) {
            sql += " OFFSET " + offset;
        }

        return sql;
    }

}
