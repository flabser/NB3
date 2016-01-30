package kz.flabs.dataengine.postgresql.queryformula;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.h2.queryformula.SelectFormula;
import kz.flabs.parser.Block;
import kz.flabs.parser.FieldExpression;
import kz.flabs.parser.FormulaBlockType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.runtimeobj.constants.SortingType;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.RunTimeParameters.Filter;
import kz.flabs.users.RunTimeParameters.Sorting;
import kz.flabs.users.User;

import java.util.ArrayList;
import java.util.Set;

public class GlossarySelectFormula implements ISelectFormula {
    protected FormulaBlocks preparedBlocks;


    public GlossarySelectFormula(FormulaBlocks preparedBlocks){
        this.preparedBlocks = preparedBlocks;
    }

    @Override
    public String getCountForPaging(Set<String> users, Set<Filter> filters) {
        return null;
    }

    public String getCondition(Set<String> complexUserID, int pageSize, int offset, String[] filters, String[] sorting, boolean checkResponse){
        String cuID = DatabaseUtil.prepareListToQuery(complexUserID), ij = "", cc = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();
        for(Condition c:conds){
            ij += 	"INNER JOIN CUSTOM_FIELDS_GLOSSARY " + c.alias + " ON GLOSSARY.DOCID = " + c.alias + ".DOCID ";
            cc += c.formula;
        }
        if (!cc.equalsIgnoreCase("")){
            cc = " AND " + cc;
        }
        String sortingColumns = getSortingColumns(sorting);
        String sql = "SELECT foo2.count, " + getResponseCondition(checkResponse, cuID) +
                " GLOSSARY.DDBID, GLOSSARY.DOCID, GLOSSARY.DOCTYPE, GLOSSARY.HAS_ATTACHMENT, GLOSSARY.VIEWTEXT, GLOSSARY.FORM," +
                " GLOSSARY.REGDATE, " + DatabaseUtil.getViewTextList("GLOSSARY") + ", GLOSSARY.VIEWNUMBER, GLOSSARY.VIEWDATE FROM GLOSSARY" +
                " INNER JOIN (" +
                " SELECT DISTINCT foo.count, " + getResponseCondition(checkResponse, cuID) +
                " GLOSSARY.DOCID " + (sortingColumns.length() > 0 ? ", " + sortingColumns : "") + " FROM GLOSSARY" +
                ij +
                ", (SELECT count(DISTINCT GLOSSARY.DOCID) FROM GLOSSARY " + ij +
                " WHERE " + sysCond + cc + ") as foo" +
                " WHERE " + sysCond +
                " " + cc  + getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset) + " ) as foo2 on foo2.docid = GLOSSARY.docid " + getOrderCondition(sorting);
        return sql;
    }

    public String getSortingColumns(String[] sorting) {
        String value = "";
        for (String col : sorting) {
            if (col != null && "".equalsIgnoreCase(col)) {
                value += "GLOSSARY." + col + ", ";
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
                value += "GLOSSARY." + col.getName() + ", ";
            }
        }
        if (value.length() > 2) {
            value = value.substring(0, value.length()-2);
        }
        return value;
    }

    @Override
    public String getCountCondition(Set<String> complexUserID,String[] filters) {
        String cuID = DatabaseUtil.prepareListToQuery(complexUserID), ij = "", cc = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();
        for(Condition c:conds){
            ij += 	"INNER JOIN CUSTOM_FIELDS_GLOSSARY " + c.alias + " ON GLOSSARY.DOCID = " + c.alias + ".DOCID ";
            cc += c.formula;
        }
        if (!cc.equalsIgnoreCase("")){
            cc = " AND " + cc;
        }

        String sql = "SELECT count(DISTINCT GLOSSARY.DOCID) FROM GLOSSARY " + ij +
                " WHERE " + sysCond + cc + ";";
        return sql;
    }

    protected ArrayList<Condition> getConditions(){
        ArrayList<Condition> conditions = new ArrayList<>();
        int condNum = 1;
        for (Block part : preparedBlocks.blocks) {
            if (part.blockType == FormulaBlockType.CONDITION){
                String alias = "cf" + condNum;
                conditions.add(new Condition(alias, part.getContent().replace("CUSTOM_FIELDS", alias)));
                condNum ++ ;
            }
        }
        return conditions;

    }

    protected String getSystemConditions(Set<Filter> filters){
        String  where = "";
        boolean and = false;
        for (Block part : preparedBlocks.blocks) {
            if (part.blockType == FormulaBlockType.SYSTEM_FIELD_CONDITION){
                where += part.getContent();
            }
        }

        if(filters.size() > 0 && where.trim().length() > 0 && !where.trim().toUpperCase().endsWith("AND"))
            where += " AND ";
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
                where += cond + " AND ";
            }
        }

     /*   if (where.trim().length() != 0 && (!where.toUpperCase().trim().endsWith("AND"))){
            where = where + "AND";
        }*/
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

    protected String getResponseCondition(boolean responseCheck, String responseQueryCondition) {
        String sql = "";
        if (responseCheck) {
            sql = " case " +
                    " when exists( " +
                    "      select 1 " +
                    "      from GLOSSARY " +
                    "      where " +
                    "            GLOSSARY.parentdocid = mdocs.DOCID and " +
                    "            GLOSSARY.parentdoctype = mdocs.DOCTYPE " +
                    "  ) then 1  else 0 " +
                    " end as parent_exists,";
        }
        return sql;
    }

    protected String getResponseCondition(boolean responseCheck) {
        String sql = "";
        if (responseCheck) {
            sql = " case " +
                    " when exists( " +
                    "      select 1 " +
                    "      from GLOSSARY " +
                    "      where " +
                    "            GLOSSARY.parentdocid = mdocs.DOCID and " +
                    "            GLOSSARY.parentdoctype = mdocs.DOCTYPE " +
                    "  ) then 1  else 0 " +
                    " end as parent_exists,";
        }
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
            return " ORDER BY DOCID ASC";
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
            return " ORDER BY DOCID ASC";
        }
    }

    protected String getPagingCondition(int pageSize, int offset){
        String pageSQL = "";
        if (pageSize == -1) {
            return pageSQL;
        }
        if (pageSize > 0 ) {
            pageSQL += " LIMIT " + pageSize;
        }
        if (offset > 0) {
            pageSQL += " OFFSET " + offset;
        }

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
        String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();

        for(Condition c:conds){
            String exCond = c.formula;
            if(exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS_GLOSSARY " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }

        String sql =
                " SELECT foo.count, " + getResponseCondition(checkResponse, responseQueryCondition) +
                        "     mdocs.DDBID, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM," +
                        "     mdocs.REGDATE, " + DatabaseUtil.getViewTextList("mdocs") + ", mdocs.VIEWNUMBER, mdocs.VIEWDATE " +
                        " FROM GLOSSARY mdocs, " +
                        " (SELECT count(md.DOCID) as count FROM GLOSSARY md " +
                        " WHERE " + sysCond.replaceAll("GLOSSARY.", "md.") +
                        existCond + ") as foo " +

                        " WHERE " + sysCond.replaceAll("GLOSSARY.", "mdocs.") +
                        existCond.replace("md.", "mdocs.") + " " + getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

    @Override
    public String getCountCondition(User user, Set<Filter> filters, SelectFormula.ReadCondition readCondition) {
        return null;
    }

    @Override
    public String getCountCondition(User user, Set<Filter> filters, SelectFormula.ReadCondition readCondition, String customFieldName) {
        return null;
    }

    @Override
    public String getCondition(Set<String> complexUserID, int pageSize,	int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse) {

        for (Block block : preparedBlocks.blocks) {
            for (FieldExpression fieldExpression : block.getExpressionList()) {
                if ("form".equalsIgnoreCase(fieldExpression.fieldName) && "account".equalsIgnoreCase(fieldExpression.fieldValue)) {
                    return "select (select count(id) from account), *, id as docid, type as doctype, null as ddbid, 'account' as form, 0 as topicid,  0 as has_attachment, name as viewtext, name as viewtext1, owner as viewtext2, array_to_string(observers, ',', '') as viewtext3, name as viewtext4,\n" +
                            "name as viewtext5, name as viewtext6, name as viewtext7, amountcontrol as viewnumber, now() as viewdate from account";
                }
            }
        }

        String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();

        for(Condition c:conds){
            String exCond = c.formula;
            if(exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS_GLOSSARY " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }

        if(existCond.trim().toLowerCase().startsWith("and")) {
            existCond = existCond.trim().substring(3, existCond.trim().length());
        }

        String sql =
                " SELECT foo.count, " + getResponseCondition(checkResponse, cuID) +
                        "     mdocs.DDBID, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM," +
                        "     mdocs.REGDATE, " + DatabaseUtil.getViewTextList("mdocs") + ", mdocs.VIEWNUMBER, mdocs.VIEWDATE, mdocs.TOPICID " +
                        " FROM GLOSSARY mdocs, " +
                        " (SELECT count(md.DOCID) as count FROM GLOSSARY md " +
                        (!"".equalsIgnoreCase(sysCond+existCond) ? " WHERE " : "") +
                        sysCond.replaceAll("GLOSSARY.", "md.") +
                        (existCond) + ") as foo " +
                        (!"".equalsIgnoreCase(sysCond+existCond) ? " WHERE " : "") +
                        sysCond.replaceAll("GLOSSARY.", "mdocs.") +
                        existCond.replace("md.", "mdocs.") + " " + getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

    @Override
    public String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, boolean checkRead) {
        return null;
    }

    @Override
    public String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, SelectFormula.ReadCondition condition) {
        return null;
    }

    @Override
    public String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, SelectFormula.ReadCondition condition, String customFieldName) {
        return null;
    }

    @Override
    public String getCountCondition(Set<String> complexUserID, Set<Filter> filters) {

        for (Block block : preparedBlocks.blocks) {
            for (FieldExpression fieldExpression : block.getExpressionList()) {
                if ("form".equalsIgnoreCase(fieldExpression.fieldName) && "account".equalsIgnoreCase(fieldExpression.fieldValue)) {
                    return "select count(*) from account";
                }
            }
        }

        String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();
        for(Condition c:conds){
            String exCond = c.formula;
            if(exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS_GLOSSARY " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }
        if(existCond.trim().toLowerCase().startsWith("and")) {
            existCond = existCond.trim().substring(3, existCond.trim().length());
        }
        String sql = "SELECT count(md.DOCID) as count FROM GLOSSARY md" +
                (!"".equalsIgnoreCase(sysCond+existCond) ? " WHERE " : "") +
                sysCond.replaceAll("GLOSSARY.", "md.") +
                existCond + ";";
        return sql;
    }



}
