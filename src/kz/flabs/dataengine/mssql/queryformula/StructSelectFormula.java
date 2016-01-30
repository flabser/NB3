package kz.flabs.dataengine.mssql.queryformula;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlockType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.runtimeobj.constants.SortingType;
import kz.flabs.users.RunTimeParameters.Filter;
import kz.flabs.users.RunTimeParameters.Sorting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Set;

public class StructSelectFormula extends kz.flabs.dataengine.h2.queryformula.StructSelectFormula {


    public StructSelectFormula(FormulaBlocks preparedBlocks) {
        super(preparedBlocks);
    }

    public String getSortingColumns(String[] sorting) {
        String value = "";
        for (String col : sorting) {
            if (col != null && "".equalsIgnoreCase(col)) {
                value += "StructureCollection." + col + ", ";
            }
        }
        if (value.length() > 2) {
            value = value.substring(0, value.length()-2);
        }
        return value;
    }

    public String getSortingColumns(ArrayList<Sorting> sorting) {
        String value = "";
        for (Sorting col : sorting) {
            if (col != null && !"".equalsIgnoreCase(col.getName())) {
                value += "StructureCollection." + col.getName() + ", ";
            }
        }
        if (value.length() > 2) {
            value = value.substring(0, value.length()-2);
        }
        return value;
    }

    @Override
    public String getCondition(Set<String> complexUserID, int pageSize, int offset, String[] filters, String[] sorting, boolean checkResponse) {
        String sysCond = getSystemConditions(filters);
        sysCond = StringUtils.removeEnd(sysCond.trim(), "and");
        String sql =
                " SELECT foo.count, " + getResponseCondition(checkResponse, "") +
                        " orgs.empid, orgs.depid, orgs.orgid, orgs.regdate, orgs.author, orgs.doctype, " +
                        " orgs.parentdocid, orgs.parentdoctype, orgs.viewtext, orgs.ddbid, orgs.form, " +
                        " orgs.fullname, orgs.shortname, orgs.address, orgs.defaultserver, orgs.comment, " +
                        " orgs.ismain, orgs.bin, orgs.hits, orgs.indexnumber, orgs.rank, orgs.type, orgs.userid, " +
                        " orgs.post, orgs.phone, orgs.birthdate, orgs.viewtext1, orgs.viewtext2, orgs.viewtext3, " +
                        " orgs.viewtext4, orgs.viewtext5, orgs.viewtext6, orgs.viewtext7, orgs.viewnumber, orgs.viewdate, null as topicid " +
                        " FROM StructureCollection orgs, " +
                        " (SELECT count(sc.docid) as count FROM StructureCollection o " +
                        " WHERE " + sysCond.replaceAll("organizations.", "sc.") + ") as foo " +
                        " WHERE " + sysCond.replaceAll("organizations.", "sc.") +
                        getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

    @Override
	public String getCountCondition(Set<String> complexUserID,String[] filters) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), ij = "", cc = "";
		String sysCond = getSystemConditions(filters);

		String sql = "SELECT count(DISTINCT StructureCollection.ORGID) FROM StructureCollection "  +
				" WHERE " + sysCond + ";";
		return sql;
	}

    protected String getSystemConditions(Set<Filter> filters){
        String  where = "";
        boolean and = false;
        for (Block part : preparedBlocks.blocks) {
            if (part.blockType == FormulaBlockType.SYSTEM_FIELD_CONDITION){
                where += part.getContent();
            }
        }
        
        if(where.trim().length() > 0 && !where.trim().toLowerCase().endsWith("and"))
        	where += " and ";
        for (Filter filter : filters) {
            String cond = "";
            if (filter != null && filter.getName() != null && filter.keyWord != null) {
                cond = filter.getName();
                switch (filter.fieldType) {
                    case TEXT:
                        cond += " LIKE '%" + filter.keyWord + "%' ";
                        break;
                    case DATE:
                    case DATETIME:
                        cond += " = '" + filter.keyWord + "' ";
                        break;
                    case NUMBER:
                        cond += " = " + filter.keyWord + " ";
                        break;
                    default:
                        cond += " LIKE '%" + filter.keyWord + "%' ";
                        break;
                }
                and = true;
            }
            if (and) {
                where += cond + " and ";
            }
        }

        if (where.trim().length() != 0 && (!where.trim().endsWith("AND")) && (!where.trim().endsWith("and"))){
            where = where + "AND";
        }
        return where;
    }

	protected String getSystemConditions(String[] filters){
		String  where = "";
		for (Block part : preparedBlocks.blocks) {	
			if (part.blockType == FormulaBlockType.SYSTEM_FIELD_CONDITION){				
				where += part.getContent();
			}				
		}

        for (int i = 0; i < filters.length; i++) {
            String cond = filters[i];
            boolean and = false;
            if (cond != null && !"".equalsIgnoreCase(cond) && !"null".equalsIgnoreCase(cond)) {
                and = false;
                switch (i) {
                    case 0:
                        cond = "VIEWTEXT LIKE '%" + cond + "%' ";
                        break;
                    case 1:
                    case 2:
                    case 3:
                        cond = "VIEWTEXT" + i + " LIKE '%" + cond + "%' ";
                        break;
                    case 4:
                        cond = "VIEWNUMBER = " + cond + " ";
                        break;
                    case 5:
                        cond = "VIEWDATE = '" + cond + "' ";
                        break;
                    default:
                        cond = "VIEWTEXT LIKE '%" + cond + "%'";
                        break;
                }
                and = true;
            }
            if (and) {
                where += cond + " and ";
            }
        }

		if ((!where.trim().endsWith("AND")) && (!where.trim().endsWith("and"))){
			where = where + "AND";
		}
		return where;	
	}

    protected String getResponseCondition(boolean responseCheck, String cuID, String responseQueryCondition) {
        String sql = "";
  /*      if (responseCheck) {
            sql = " case " +
                    " when exists( " +
                    "      select 1 " +
                    "      from MAINDOCS " +
                    "      inner join readers_maindocs " +
                    "          on MAINDOCS.DOCID = READERS_MAINDOCS.DOCID " +
                    "      where readers_maindocs.username in (" + cuID + ") and " +
                    "            MAINDOCS.form != 'discussion' and " +
                    "            MAINDOCS.parentdocid = mdocs.DOCID and " +
                    "            MAINDOCS.parentdoctype = mdocs.DOCTYPE " +
                                (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")  +
                    "  ) or exists( " +
                    "      select 1 " +
                    "      from tasks " +
                    "      inner join readers_tasks " +
                    "          on tasks.DOCID = readers_tasks.DOCID " +
                    "      where readers_tasks.username in (" + cuID + ") and " +
                    "            tasks.parentdocid = mdocs.DOCID and " +
                    "            tasks.parentdoctype = mdocs.DOCTYPE " +
                                (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")  +
                    "  ) or exists ( " +
                    "          select 1 " +
                    "      from executions " +
                    "      inner join readers_executions " +
                    "          on executions.DOCID = readers_executions.DOCID " +
                    "      where readers_executions.username in (" + cuID + ") and " +
                    "            executions.parentdocid = mdocs.DOCID and " +
                    "            executions.parentdoctype = mdocs.DOCTYPE " +
                                (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")  +
                    "  ) then 1  else 0 " +
                    " end as parent_exists,";
        }*/
        return sql;
    }

    protected String getResponseCondition(boolean responseCheck, String cuID) {
		String sql = "";
/*		if (responseCheck) {
			sql = " case " +
			      " when exists( " +
		          "      select 1 " +    
		          "      from MAINDOCS " +    
    		      "      inner join readers_maindocs " +  
    		      "          on MAINDOCS.DOCID = READERS_MAINDOCS.DOCID " +    
    		      "      where readers_maindocs.username in (" + cuID + ") and " + 
    		      "            MAINDOCS.form != 'discussion' and " +
    		      "            MAINDOCS.parentdocid = mdocs.DOCID and " +
    		      "            MAINDOCS.parentdoctype = mdocs.DOCTYPE " +
    		      "  ) or exists( " +
    		      "      select 1 " +    
    		      "      from tasks " +    
    		      "      inner join readers_tasks " +  
    		      "          on tasks.DOCID = readers_tasks.DOCID " +    
    		      "      where readers_tasks.username in (" + cuID + ") and " +
    		      "            tasks.parentdocid = mdocs.DOCID and " +
    		      "            tasks.parentdoctype = mdocs.DOCTYPE " +         
    		      "  ) or exists ( " +
    		      "          select 1 " +   
    		      "      from executions " +    
    		      "      inner join readers_executions " +  
    		      "          on executions.DOCID = readers_executions.DOCID " +    
    		      "      where readers_executions.username in (" + cuID + ") and " + 
    		      "            executions.parentdocid = mdocs.DOCID and " +
    		      "            executions.parentdoctype = mdocs.DOCTYPE " + 
    		      "  ) then 1  else 0 " +
	              " end as parent_exists,";
		} */
		return sql;
	}

    protected String getOrderCondition(String[] sorting) {
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
            return " ORDER BY ORGID ASC";
        }
    }

    protected String getOrderCondition(Set<Sorting> sorting) {
        String tmpsql = "";

        for (Sorting sort : sorting) {
            String cond = "";
            boolean and = false;
            if (sort != null && sort.getName() != null && sort.sortingDirection != SortingType.UNSORTED) {
                cond = sort.getName() + " " + sort.sortingDirection.toString() + " ";
                and = true;
            }
            if (and) {
                tmpsql += cond + ",";
            }
        }

        if (!"".equalsIgnoreCase(tmpsql)) {
            return " ORDER BY " + tmpsql.substring(0, tmpsql.length() - 1);
        } else {
            return " ORDER BY ORGID ASC";
        }
    }


    protected String getPagingCondition(int pageSize, int offset){
		String pageSQL = "";
/*        if (pageSize == -1) {
            return pageSQL;
        }
        if (pageSize > 0 ) {
			pageSQL += " LIMIT " + pageSize;
        }
        if (offset > 0) {
            pageSQL += " OFFSET " + offset;
        }*/

		return pageSQL;

	}


	class Condition{
		String alias;
		String formula;


		public Condition(String a, String f) {
			alias = a;
			formula = f;
		}

		public String toString(){
			return "alias=" + alias + ", formula=" + formula;
		}
	}

    public String getCondition(Set<String> complexUserID, int pageSize,	int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, String responseQueryCondition) {
        String sysCond = getSystemConditions(filters);
        sysCond = StringUtils.removeEnd(sysCond.trim(), "and");
        String sql =
                " SELECT foo.count, " + getResponseCondition(checkResponse, "", responseQueryCondition) +
                        "     orgs.DDBID, orgs.ORGID, orgs.DOCID, orgs.DOCTYPE, 0 as has_attachment, null as topicid, orgs.VIEWTEXT, orgs.FORM," +
                        "     orgs.REGDATE, " + DatabaseUtil.getViewTextList("orgs") + ", orgs.VIEWNUMBER, orgs.VIEWDATE " +
                        " FROM StructureCollection orgs, " +
                        " (SELECT count(sc.docid) as count FROM StructureCollection sc " +
                        " WHERE " + sysCond.replaceAll("organizations.", "sc.") + ") as foo " +
                        " WHERE " + sysCond.replaceAll("organizations.", "sc.") +
                        getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

	@Override
	public String getCondition(Set<String> complexUserID, int pageSize,	int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse) {
        return this.getCondition(complexUserID, pageSize, offset, filters, sorting, checkResponse, "");
	}

	@Override
	public String getCountCondition(Set<String> complexUserID, Set<Filter> filters) {
        String sysCond = getSystemConditions(filters);

        String sql = "SELECT count(md.DOCID) as count FROM StructureCollection md" +
                " WHERE " + sysCond.replaceAll("maindocs.", "md.");
        return sql;
	}


	
}
