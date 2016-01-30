package kz.flabs.dataengine.mssql.queryformula;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.exception.DataConversionException;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlockType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.util.Util;

import java.util.Date;
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

	public boolean isSortBy() {
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

	public SortByBlock getSortBlock(){
		return this.preparedBlocks.getSortByBlock();
	}

	public String getGroupCondition(String value){		
		String whereSQLpart = " AND MAINDOCS.DOCID IN (select MAINDOCS.docid from MAINDOCS, CUSTOM_FIELDS "
				+ "WHERE MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID AND (" + preparedBlocks.getGroupByBlock().getGroup() + " AND " + preparedBlocks.getGroupByBlock().getCondition(value) + ")) AND "; 		  		
		whereSQLpart = getConditions(whereSQLpart);		
		return whereSQLpart;		
	}

	public String getSQL(Set<String> complexUserID) { 
		String fieldsSQLpart = " DISTINCT MAINDOCS.DDBID, MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM, MAINDOCS.REGDATE ";
		/*String sql = "SELECT ", fromSQLpart = "MAINDOCS, READERS_MAINDOCS",
                whereSQLpart = " WHERE MAINDOCS.DOCID=READERS_MAINDOCS.DOCID "
                        + "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/

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
			whereSQLpart += " AnD ";
		}

		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;	

		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		}else{
			if (preparedBlocks.isSortBy){
				SortByBlock sortBlock = preparedBlocks.getSortByBlock();
				sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
			}
		}		
		return sql;
		/*		String fieldsSQLpart = " * ";
		String sql = "SELECT ", fromSQLpart = "MAINDOCS ",
			   whereSQLpart = " WHERE READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ "and MAINDOCS.DOCID=READERS_MAINDOCS.DOCID ",
			   existsSubQuery = " WHERE EXISTS (SELECT * FROM READERS_MAINDOCS ";

		if (preparedBlocks.isGroupBy){
			fieldsSQLpart = preparedBlocks.getGroupByBlock().operator + ",count(" + preparedBlocks.getGroupByBlock().operator + ")";
			fromSQLpart += ", CUSTOM_FIELDS ";			
			whereSQLpart += " AND MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID and " + preparedBlocks.getGroupByBlock().getGroup() + " AND ";
		}else{
			whereSQLpart += " AND ";
		}

		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + existsSubQuery + whereSQLpart + ")";	

		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		}else{
			sql += " ORDER BY MAINDOCS.REGDATE ";
		}		
		return sql;*/
	}

	public String getSQLCount(Set<String> complexUserID) {
		String fieldsSQLpart = " count(DISTINCT MAINDOCS.DOCID) ";
		/*String sql = "SELECT ", fromSQLpart = "MAINDOCS, READERS_MAINDOCS",
                whereSQLpart = " WHERE MAINDOCS.DOCID=READERS_MAINDOCS.DOCID "
                        + "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";*/

		String sql = "SELECT ", fromSQLpart = "MAINDOCS",
				whereSQLpart = " WHERE MAINDOCS.DOCID IN (select docid from READERS_MAINDOCS where" +
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

		whereSQLpart += " AND ";
		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;

		return sql;
		/*		String fieldsSQLpart = " count(*) ";
		String sql = "SELECT ", fromSQLpart = "MAINDOCS ",
				existsSubQuery = " WHERE EXISTS (SELECT * FROM READERS_MAINDOCS ", 
				whereSQLpart = " WHERE READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ "and READERS_MAINDOCS.DOCID=MAINDOCS.DOCID ";

		whereSQLpart += " AND ";
		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + existsSubQuery + whereSQLpart + ")";

		return sql;*/

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
	public String getSQLGroupCount(Set<String> complexUserID) {
		String fieldsSQLpart = " DISTINCT MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.DDBID, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM, MAINDOCS.REGDATE ";

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
			whereSQLpart += " AnD ";
		}

		whereSQLpart = getConditions(whereSQLpart);		

		sql += fieldsSQLpart + " FROM " + fromSQLpart + whereSQLpart;	

		if (preparedBlocks.isGroupBy){
			sql += " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
		}else{
			if (preparedBlocks.isSortBy){
				SortByBlock sortBlock = preparedBlocks.getSortByBlock();
				sql += " ORDER BY " + (sortBlock != null ? sortBlock.fieldName : Const.DEFAULT_SORT_COLUMN) + " " + (sortBlock != null ? sortBlock.order : Const.DEFAULT_SORT_ORDER);
			}
		}
		sql = "SELECT COUNT(*) FROM (" + sql + ") groups";
		return sql;
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

	@Override
	public FormulaBlocks getBlocks() {
		return preparedBlocks;
	}

    @Override
    public String getSQL(Set<String> complexUserID, int limit, int offset, String[] filters, String[] sorting) {
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
                        Date condDate = Util.convertStringToDateTime(cond);
                        try{
                            cond = Util.convertDateTimeToDerbyFormat(condDate);
                            cond = "VIEWDATE = '" + cond + "' ";
                        } catch (DataConversionException e) {
                            e.printStackTrace();
                            continue;
                        }
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

        sql += fieldsSQLpart + " FROM " + fromSQLpart;
        sql += whereSQLpart;

        String groupSQLpart = "";
        if (preparedBlocks.isGroupBy){
            groupSQLpart = " GROUP BY " + preparedBlocks.getGroupByBlock().operator;
            sql += groupSQLpart;
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
        String ordersql = "";
        if (!"".equalsIgnoreCase(tmpsql)) {
            ordersql += " ORDER BY " + tmpsql.substring(0, tmpsql.length()-1);
        }
        sql += ordersql;

        String sqlWithPaging = " DECLARE @intStartRow int; " +
                " DECLARE @intEndRow int; " +
                " DECLARE @intPage int = " + offset + ";" +
                " DECLARE @intPageSize int = " + limit + ";" +
                " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
                " SET @intEndRow = @intPage * @intPageSize;" +
                " WITH blogs AS" +
                " (SELECT " + fieldsSQLpart + ", " +
                " ROW_NUMBER() OVER(" + ordersql + ") as intRow, " +
                " COUNT(" +  Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " +
                " FROM " + fromSQLpart + " " + whereSQLpart + " ) " +
                " SELECT " + fieldsSQLpart + " FROM blogs" +
                " WHERE intRow BETWEEN @intStartRow AND @intEndRow" + groupSQLpart;

        if (limit == -1 && --offset == 0) {
            return sql;
        } else {
            return sqlWithPaging;
        }
    }
}
