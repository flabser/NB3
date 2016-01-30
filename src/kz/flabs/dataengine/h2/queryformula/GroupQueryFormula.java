package kz.flabs.dataengine.h2.queryformula;

import java.util.Set;
import kz.flabs.parser.FormulaBlocks;


public class GroupQueryFormula extends QueryFormula{
	public String queryID;

	public GroupQueryFormula(String queryID, FormulaBlocks preparedBlocks){
		super(queryID, preparedBlocks);
		this.queryID = queryID;
		
	}

	@Override
	public String getSQLGroupCount(Set<String> complexUserID) {		
		String sql = "SELECT COUNT(*) FROM GROUPS";
		return sql;
	}

	public String getGroupCondition(String value){		
		return "";		
	}

	public String getSQL(Set<String> complexUserID) {
		String sql = "SELECT * FROM GROUPS";
		return sql;
	}

	public String getSQLCount(Set<String> complexUserID) {
		String sql = "SELECT COUNT(*) FROM GROUPS";
		return sql;
	}	

	@Override
	public String getSQL(Set<String> complexUserID, int pageSize, int offset) {
		String sql = "SELECT * FROM GROUPS";

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
		String sql = "SELECT * FROM GROUPS";

		if (pageSize > 0) {
			sql += " LIMIT " + pageSize;
		}
		if (offset > 0) {
			sql += " OFFSET " + offset;
		}

		return sql;
	}

}
