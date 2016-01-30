package kz.flabs.dataengine.postgresql.queryformula;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlockType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.runtimeobj.constants.SortingType;
import kz.flabs.users.RunTimeParameters.Filter;
import kz.flabs.users.RunTimeParameters.Sorting;
import kz.flabs.users.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static kz.flabs.dataengine.h2.queryformula.SelectFormula.ReadCondition;


public class SelectFormula implements ISelectFormula {

	protected FormulaBlocks preparedBlocks;

	public SelectFormula(FormulaBlocks preparedBlocks) {
		this.preparedBlocks = preparedBlocks;
	}

	public String getCondition(Set <String> complexUserID, int pageSize, int offset, String[] filters,
			String[] sorting, boolean checkResponse) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), ij = "", cc = "";
		String sysCond = getSystemConditions(filters);
		ArrayList <Condition> conds = getConditions();
		for (Condition c : conds) {
			ij += "INNER JOIN CUSTOM_FIELDS " + c.alias + " ON MAINDOCS.DOCID = " + c.alias + ".DOCID ";
			cc += c.formula;
		}
		if (!cc.equalsIgnoreCase("")) {
			cc = " AND " + cc;
		}
		String sortingColumns = getSortingColumns(sorting);
		String sql = "SELECT foo2.count, "
				+ getResponseCondition(checkResponse, cuID)
				+ " MAINDOCS.DDBID, MAINDOCS.DOCID, MAINDOCS.DOCTYPE, MAINDOCS.HAS_ATTACHMENT, MAINDOCS.VIEWTEXT, MAINDOCS.FORM,"
				+ " MAINDOCS.REGDATE, "
				+ DatabaseUtil.getViewTextList("MAINDOCS")
				+ ", MAINDOCS.VIEWNUMBER, MAINDOCS.VIEWDATE FROM MAINDOCS"
				+ " INNER JOIN ("
				+ " SELECT DISTINCT foo.count, "
				+ getResponseCondition(checkResponse, cuID)
				+ " MAINDOCS.DOCID "
				+ (sortingColumns.length() > 0 ? ", " + sortingColumns : "")
				+ " FROM MAINDOCS"
				+ " INNER JOIN READERS_MAINDOCS ON MAINDOCS.DOCID = READERS_MAINDOCS.DOCID  "
				+ ij
				+ ", (SELECT count(DISTINCT MAINDOCS.DOCID) FROM MAINDOCS INNER JOIN READERS_MAINDOCS ON MAINDOCS.DOCID = READERS_MAINDOCS.DOCID "
				+ ij + " WHERE " + sysCond + " READERS_MAINDOCS.USERNAME IN (" + cuID + ")" + cc + ") as fo"
				+ "o WHERE " + sysCond + " READERS_MAINDOCS.USERNAME IN (" + cuID + ")" + cc
				+ getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset)
				+ " ) as foo2 on foo2.docid = maindocs.docid " + getOrderCondition(sorting);
		return sql;
	}

	public String getSortingColumns(String[] sorting) {
		String value = "";
		for (String col : sorting) {
			if (col != null && "".equalsIgnoreCase(col)) {
				value += "MAINDOCS." + col + ", ";
			}
		}
		if (value.length() > 2) {
			value = value.substring(0, value.length() - 2);
		}
		return value;
	}

	public String getSortingColumns(ArrayList <Sorting> sorting) {
		String value = "";
		for (Sorting col : sorting) {
			if (col != null && !"".equalsIgnoreCase(col.getName())) {
				value += "MAINDOCS." + col.getName() + ", ";
			}
		}
		if (value.length() > 2) {
			value = value.substring(0, value.length() - 2);
		}
		return value;
	}

	@Override
	public String getCountCondition(Set <String> complexUserID, String[] filters) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), ij = "", cc = "";
		String sysCond = getSystemConditions(filters);
		ArrayList <Condition> conds = getConditions();
		for (Condition c : conds) {
			ij += "INNER JOIN CUSTOM_FIELDS " + c.alias + " ON MAINDOCS.DOCID = " + c.alias + ".DOCID ";
			cc += c.formula;
		}
		if (!cc.equalsIgnoreCase("")) {
			cc = " AND " + cc;
		}

		String sql = "SELECT count(DISTINCT MAINDOCS.DOCID) FROM MAINDOCS INNER JOIN READERS_MAINDOCS ON MAINDOCS.DOCID = READERS_MAINDOCS.DOCID "
				+ ij + " WHERE " + sysCond + " READERS_MAINDOCS.USERNAME IN (" + cuID + ")" + cc + ";";
		return sql;
	}

	protected ArrayList <Condition> getConditions() {
		ArrayList <Condition> conditions = new ArrayList <Condition>();
		int condNum = 1;
		for (Block part : preparedBlocks.blocks) {
			if (part.blockType == FormulaBlockType.CONDITION) {
				String alias = "cf" + condNum;
				conditions.add(new Condition(alias, part.getContent().replace("CUSTOM_FIELDS", alias)));
				condNum++;
			}
		}
		return conditions;
	}

	protected String getSystemConditions(Set <Filter> filters) {
		String where = "";
		boolean and = false;
		for (Block part : preparedBlocks.blocks) {
			if (part.blockType == FormulaBlockType.SYSTEM_FIELD_CONDITION) {
				where += part.getContent();
			}
		}

		if (where.trim().length() > 0 && !where.trim().toLowerCase().endsWith("and")) where += " and ";
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

		if (where.trim().length() != 0 && (!where.trim().endsWith("AND")) && (!where.trim().endsWith("and"))) {
			where = where + "AND";
		}
		return where;
	}

	protected String getSystemConditions(String[] filters) {
		String where = "";
		for (Block part : preparedBlocks.blocks) {
			if (part.blockType == FormulaBlockType.SYSTEM_FIELD_CONDITION) {
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

		if ((!where.trim().endsWith("AND")) && (!where.trim().endsWith("and"))) {
			where = where + "AND";
		}
		return where;
	}

	protected String getResponseCondition(boolean responseCheck, String cuID, String responseQueryCondition) {
		String sql = "";
		if (responseCheck) {
			sql = " case " + " when exists( " + "      select 1 " + "      from MAINDOCS "
					+ "      inner join readers_maindocs " + "          on MAINDOCS.DOCID = READERS_MAINDOCS.DOCID "
					+ "      where readers_maindocs.username in ("
					+ cuID
					+ ") and "
					+ "            MAINDOCS.form != 'discussion' and "
					+ "            MAINDOCS.parentdocid = mdocs.DOCID and "
					+ "            MAINDOCS.parentdoctype = mdocs.DOCTYPE "
					+ (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
					+ "  ) or exists( "
					+ "      select 1 "
					+ "      from tasks "
					+ "      inner join readers_tasks "
					+ "          on tasks.DOCID = readers_tasks.DOCID "
					+ "      where readers_tasks.username in ("
					+ cuID
					+ ") and "
					+ "            tasks.parentdocid = mdocs.DOCID and "
					+ "            tasks.parentdoctype = mdocs.DOCTYPE "
					+ (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
					+ "  ) or exists ( "
					+ "          select 1 "
					+ "      from executions "
					+ "      inner join readers_executions "
					+ "          on executions.DOCID = readers_executions.DOCID "
					+ "      where readers_executions.username in ("
					+ cuID
					+ ") and "
					+ "            executions.parentdocid = mdocs.DOCID and "
					+ "            executions.parentdoctype = mdocs.DOCTYPE "
					+ (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
					+ "  ) then 1  else 0 " + " end as parent_exists,";
		}
		return sql;
	}

	protected String getResponseCondition(boolean responseCheck, String cuID) {
		String sql = "";
		if (responseCheck) {
			sql = " case " + " when exists( " + "      select 1 " + "      from MAINDOCS "
					+ "      inner join readers_maindocs " + "          on MAINDOCS.DOCID = READERS_MAINDOCS.DOCID "
					+ "      where readers_maindocs.username in ("
					+ cuID
					+ ") and "
					+ "            MAINDOCS.form != 'discussion' and "
					+ "            MAINDOCS.parentdocid = mdocs.DOCID and "
					+ "            MAINDOCS.parentdoctype = mdocs.DOCTYPE "
					+ "  ) or exists( "
					+ "      select 1 "
					+ "      from tasks "
					+ "      inner join readers_tasks "
					+ "          on tasks.DOCID = readers_tasks.DOCID "
					+ "      where readers_tasks.username in ("
					+ cuID
					+ ") and "
					+ "            tasks.parentdocid = mdocs.DOCID and "
					+ "            tasks.parentdoctype = mdocs.DOCTYPE "
					+ "  ) or exists ( "
					+ "          select 1 "
					+ "      from executions "
					+ "      inner join readers_executions "
					+ "          on executions.DOCID = readers_executions.DOCID "
					+ "      where readers_executions.username in ("
					+ cuID
					+ ") and "
					+ "            executions.parentdocid = mdocs.DOCID and "
					+ "            executions.parentdoctype = mdocs.DOCTYPE "
					+ "  ) then 1  else 0 "
					+ " end as parent_exists,";
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

	protected String getOrderCondition(Set <Sorting> sorting) {
		String order = "";

		for (Sorting sort : sorting) {
			if (sort != null && sort.getName() != null && sort.sortingDirection != SortingType.UNSORTED) {
				order += sort.getName() + " " + sort.sortingDirection.toString() + ",";
			}
		}

		if (order.length() > 0) {
			return " ORDER BY " + order.substring(0, order.length() - 1);
		} else {
			return " ORDER BY DOCID ASC";
		}
	}

	protected String getPagingCondition(int pageSize, int offset) {
		String pageSQL = "";
		if (offset < 0) {
			return pageSQL;
		}
		if (pageSize > 0) {
			pageSQL += " LIMIT " + pageSize;
		}
		if (offset > 0) {
			pageSQL += " OFFSET " + offset;
		}

		return pageSQL;
	}

	class Condition {

		String alias;
		String formula;

		public Condition(String a, String f) {
			alias = a;
			formula = f;
		}

		public String toString() {
			return "alias=" + alias + ", formula=" + formula;
		}
	}

	@Override
	public String getCountForPaging(Set<String> complexUserID, Set<Filter> filters) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
		String sysCond = getSystemConditions(filters);
		ArrayList<Condition> conds = getConditions();

		for (Condition c : conds) {
			String exCond = c.formula;
			if (exCond.trim().toLowerCase().endsWith("and"))
				exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

			existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
		}
		String sql =
				" SELECT count(md.DOCID) as count FROM MAINDOCS md " +
						" WHERE " + sysCond.replaceAll("maindocs.", "md.") +
						" exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
						existCond;
		return sql;
	}

	public String getCondition(Set <String> complexUserID, int pageSize, int offset, Set <Filter> filters,
			Set <Sorting> sorting, boolean checkResponse, String responseQueryCondition) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
		String sysCond = getSystemConditions(filters);
		ArrayList <Condition> conds = getConditions();

		for (Condition c : conds) {
			String exCond = c.formula;
			if (exCond.trim().toLowerCase().endsWith("and")) exCond = exCond.trim().substring(0,
					exCond.trim().length() - 3);

			existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias
					+ ".DOCID and " + exCond + ") ";
		}

		String sql = " SELECT foo.count, mdocs.has_response, "
				+ "     mdocs.DDBID, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM,"
				+ "     mdocs.REGDATE, "
				+ DatabaseUtil.getViewTextList("mdocs")
				+ ", mdocs.VIEWNUMBER, mdocs.VIEWDATE "
				+ " FROM MAINDOCS mdocs, "
				+ " (SELECT count(md.DOCID) as count FROM MAINDOCS md "
				+ " WHERE "
				+ sysCond.replaceAll("maindocs.", "md.")
				+ " exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN ("
				+ cuID
				+ ")) "
				+ existCond
				+ ") as foo "
				+ " WHERE "
				+ sysCond.replaceAll("maindocs.", "mdocs.")
				+ " exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN ("
				+ cuID + ")) " + existCond.replace("md.", "mdocs.") + " " + getOrderCondition(sorting) + " "
				+ getPagingCondition(pageSize, offset);
		return sql;
	}

    @Override
    public String getCountCondition(User user, Set<Filter> filters, ReadCondition readCondition) {
        String cuID = DatabaseUtil.prepareListToQuery(user.getAllUserGroups()), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();
        for (Condition c : conds) {
            String exCond = c.formula;
            if (exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where mdocs.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }

        String sql = "SELECT count(mdocs.DOCID) as count FROM MAINDOCS mdocs" +
                " WHERE " + sysCond.replaceAll("maindocs.", "mdocs.") +
                " exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                existCond + getReadConditionByType(readCondition, user.getUserID()) + ";";
        return sql;
    }

	protected String getReadConditionByCustField(ReadCondition type, String custFieldName) {
		String sql;
		switch (type) {
			case ONLY_READ:
				sql = "and (select ru @> gu from (select array_agg(userid)::text[] as ru , gu from foracquaint ua \n" +
						"inner join \n" +
						"(select string_to_array(value, '#') as gu, docid from custom_fields where name = '" + custFieldName + "') as cf\n" +
						"on ua.docid = cf.docid\n" +
						"where ua.docid = mdocs.docid \n" +
						"group by gu) as foo) = true";
				break;
			case ONLY_UNREAD:
				sql = "and (select ru @> gu from (select array_agg(userid)::text[] as ru , gu from foracquaint ua \n" +
						"inner join \n" +
						"(select string_to_array(value, '#') as gu, docid from custom_fields where name = '" + custFieldName + "') as cf\n" +
						"on ua.docid = cf.docid\n" +
						"where ua.docid = mdocs.docid \n" +
						"group by gu) as foo) = false";
				break;
			case ALL:
				sql = "";
				break;
			default:
				sql = "";
				break;
		}
		return sql;
	}


	@Override
    public String getCountCondition(User user, Set<Filter> filters, ReadCondition readCondition, String customFieldName) {
		String cuID = DatabaseUtil.prepareListToQuery(user.getAllUserGroups()), existCond = "";
		String sysCond = getSystemConditions(filters);
		ArrayList<Condition> conds = getConditions();
		for (Condition c : conds) {
			String exCond = c.formula;
			if (exCond.trim().toLowerCase().endsWith("and"))
				exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

			existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where mdocs.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
		}

		String sql = "SELECT count(mdocs.DOCID) as count FROM MAINDOCS mdocs" +
				" WHERE " + sysCond.replaceAll("maindocs.", "mdocs.") +
				" exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
				existCond + getReadConditionByCustField(readCondition, customFieldName) + ";";
		return sql;
    }

    @Override
	public String getCondition(Set <String> complexUserID, int pageSize, int offset, Set <Filter> filters,
			Set <Sorting> sorting, boolean checkResponse) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
		String sysCond = getSystemConditions(filters);
		ArrayList <Condition> conds = getConditions();

		for (Condition c : conds) {
			String exCond = c.formula;
			if (exCond.trim().toLowerCase().endsWith("and")) exCond = exCond.trim().substring(0,
					exCond.trim().length() - 3);

			existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias
					+ ".DOCID and " + exCond + ") ";
		}

		OrderCondition orderCondition = new OrderCondition(sorting);

		String sql = " SELECT foo.count, " + getResponseCondition(checkResponse, cuID);

		if (orderCondition.byColumn) {
			sql += orderCondition.orderColumn;
		}

		sql += " mdocs.DDBID, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM,"
				+ " mdocs.REGDATE, "
				+ DatabaseUtil.getViewTextList("mdocs")
				+ ", mdocs.VIEWNUMBER, mdocs.VIEWDATE, mdocs.TOPICID "
				+ " FROM MAINDOCS mdocs, "
				+ " (SELECT count(md.DOCID) as count FROM MAINDOCS md "
				+ " WHERE "
				+ sysCond.replaceAll("maindocs.", "md.")
				+ " exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN ("
				+ cuID
				+ ")) "
				+ existCond
				+ ") as foo "
				+ " WHERE "
				+ sysCond.replaceAll("maindocs.", "mdocs.")
				+ " exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN ("
				+ cuID + ")) " + existCond.replace("md.", "mdocs.") + " " + orderCondition.order + " "
				+ getPagingCondition(pageSize, offset);
		return sql;
	}

    protected String getReadCondition(boolean readCheck, String userid) {
        String sql = "";
        if (readCheck) {
            sql = " case " +
                    " when exists( " +
                    "      select 1 " +
                    "      from users_activity " +
                    "      where type = " + UsersActivityType.MARKED_AS_READ.getCode() + " and " +
                    "          userid in (" + userid + ")" + " and " +
                    "          users_activity.docid = mdocs.docid " +
                    "  ) then 1  else 0 " +
                    " end as isread,";
        }
        return sql;
    }



    @Override
    public String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, boolean checkRead) {
        String cuID = DatabaseUtil.prepareListToQuery(user.getAllUserGroups()), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();

        for(Condition c:conds){
            String exCond = c.formula;
            if(exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }

        String sql =
                " SELECT foo.count, " + getReadCondition(checkRead, "'" + user.getUserID() + "'") +
                        "     mdocs.DDBID, mdocs.has_response, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM," +
                        "     mdocs.REGDATE, " + DatabaseUtil.getViewTextList("mdocs") + ", mdocs.VIEWNUMBER, mdocs.VIEWDATE, mdocs.TOPICID " +
                        " FROM MAINDOCS mdocs, " +
                        " (SELECT count(md.DOCID) as count FROM MAINDOCS md " +
                        " WHERE " + sysCond.replaceAll("maindocs.", "md.") +
                        " exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                        existCond + ") as foo " +

                        " WHERE " + sysCond.replaceAll("maindocs.", "mdocs.") +
                        " exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                        existCond.replace("md.", "mdocs.") + " " + getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

    @Override
    public String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, ReadCondition condition) {
        String cuID = DatabaseUtil.prepareListToQuery(user.getAllUserGroups()), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();

        for(Condition c:conds){
            String exCond = c.formula;
            if(exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }

        String sql =
                " SELECT foo.count, " +
                        "     mdocs.DDBID, mdocs.has_response, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM," +
                        "     mdocs.REGDATE, " + DatabaseUtil.getViewTextList("mdocs") + ", mdocs.VIEWNUMBER, mdocs.VIEWDATE, mdocs.TOPICID " +
                        " FROM MAINDOCS mdocs, " +
                        " (SELECT count(md.DOCID) as count FROM MAINDOCS md " +
                        " WHERE " + sysCond.replaceAll("maindocs.", "md.") +
                        " exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                        existCond + ") as foo " +

                        " WHERE " + sysCond.replaceAll("maindocs.", "mdocs.") +
                        " exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                        existCond.replace("md.", "mdocs.") + " " + getReadConditionByType(condition, user.getUserID()) + getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

    @Override
    public String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, ReadCondition condition, String customFieldName) {
        String cuID = DatabaseUtil.prepareListToQuery(user.getAllUserGroups()), existCond = "";
        String sysCond = getSystemConditions(filters);
        ArrayList<Condition> conds = getConditions();

        for (Condition c : conds) {
            String exCond = c.formula;
            if (exCond.trim().toLowerCase().endsWith("and"))
                exCond = exCond.trim().substring(0, exCond.trim().length() - 3);

            existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias + ".DOCID and " + exCond + ") ";
        }

        String sql =
                " SELECT foo.count, " +
                        "     mdocs.DDBID, mdocs.has_response, mdocs.DOCID, mdocs.DOCTYPE, mdocs.HAS_ATTACHMENT, mdocs.VIEWTEXT, mdocs.FORM," +
                        "     mdocs.REGDATE, " + DatabaseUtil.getViewTextList("mdocs") + ", mdocs.VIEWNUMBER, mdocs.VIEWDATE, mdocs.TOPICID " +
                        " FROM MAINDOCS mdocs, " +
                        " (SELECT count(md.DOCID) as count FROM MAINDOCS md " +
                        " WHERE " + sysCond.replaceAll("maindocs.", "md.") +
                        " exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                        existCond +
                        getReadConditionByCustField(condition, customFieldName).replaceAll("mdocs.", "md.") +
                        ") " +
                        " as foo " +

                        " WHERE " + sysCond.replaceAll("maindocs.", "mdocs.") +
                        " exists(select 1 from READERS_MAINDOCS where mdocs.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN (" + cuID + ")) " +
                        existCond.replace("md.", "mdocs.") + " " + getReadConditionByCustField(condition, customFieldName) + getOrderCondition(sorting) + " " + getPagingCondition(pageSize, offset);
        return sql;
    }

    protected String getReadConditionByType(ReadCondition type, String userid) {
        String sql;
        switch (type) {
            case ONLY_READ:
                sql = " and exists( " +
                        "      select 1 " +
                        "      from users_activity " +
                        "      where type = " + UsersActivityType.MARKED_AS_READ.getCode() + " and " +
                        "          userid in ('" + userid + "')" + " and " +
                        "          users_activity.docid = mdocs.docid " +
                        "  ) ";
                break;
            case ONLY_UNREAD:
                sql = " and not exists( " +
                        "      select 1 " +
                        "      from users_activity " +
                        "      where type = " + UsersActivityType.MARKED_AS_READ.getCode() + " and " +
                        "          userid in ('" + userid + "')" + " and " +
                        "          users_activity.docid = mdocs.docid " +
                        "  ) ";
                break;
            case ALL:
                sql = "";
                break;
            default:
                sql = "";
                break;
        }
        return sql;
    }

    @Override
	public String getCountCondition(Set <String> complexUserID, Set <Filter> filters) {
		String cuID = DatabaseUtil.prepareListToQuery(complexUserID), existCond = "";
		String sysCond = getSystemConditions(filters);
		ArrayList <Condition> conds = getConditions();
		for (Condition c : conds) {
			String exCond = c.formula;
			if (exCond.trim().toLowerCase().endsWith("and")) exCond = exCond.trim().substring(0,
					exCond.trim().length() - 3);

			existCond += " and exists(select 1 from CUSTOM_FIELDS " + c.alias + " where md.DOCID = " + c.alias
					+ ".DOCID and " + exCond + ") ";
		}

		String sql = "SELECT count(md.DOCID) as count FROM MAINDOCS md"
				+ " WHERE "
				+ sysCond.replaceAll("maindocs.", "md.")
				+ " exists(select 1 from READERS_MAINDOCS where md.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME IN ("
				+ cuID + ")) " + existCond + ";";
		return sql;
	}

	private class OrderCondition {

		public String order;
		public String orderColumn = "";
		public boolean byColumn = false;

		public OrderCondition(Set <Sorting> sorting) {
			List <String> orders = new ArrayList <String>();
			List <String> orderCol = new ArrayList <String>();
			String sortingDirection = "";

			for (Sorting sort : sorting) {
				if (sort != null && sort.getName() != null && sort.sortingDirection != SortingType.UNSORTED) {
					orders.add(sort.getName() + " " + sort.sortingDirection.toString());
					orderCol.add(sort.getName());
					// судя по циклу предполагалась сортировка по нескольким полям, в этом случае не
					// сработает
					if (sort.getName().indexOf("VIEWTEXT") == 0 || sort.getName().indexOf("VIEWDATE") == 0) {
						sortingDirection = sort.sortingDirection.toString();
					}
				}
			}

			if (orders.size() > 0) {
				if (sortingDirection.length() > 0) {
					orderColumn = "(" + StringUtils.join(orderCol, " || ") + " || text(DOCID)) as sort_column,";
					byColumn = true;
					order = " ORDER BY sort_column " + sortingDirection;
				} else {
					order = " ORDER BY " + StringUtils.join(orders, ",");
				}
			} else {
				order = " ORDER BY DOCID ASC";
			}
		}
	}
}
