package kz.flabs.dataengine.h2.structure;

import java.util.Set;
import kz.flabs.dataengine.h2.queryformula.QueryFormula;
import kz.flabs.parser.Block;
import kz.flabs.parser.FormulaBlocks;

public class StructQueryFormula extends QueryFormula {
	String fromSQLpart = "ORGANIZATIONS,DEPARTMENTS,EMPLOYERS e,USER_ROLES ur,ROLES r";
	String whereSQLpart = " WHERE e.EMPID = ur.EMPID AND ur.ROLEID = r.ROLEID AND ";
	String fieldsSQLpart = "*";
	
	public StructQueryFormula(String queryID, FormulaBlocks b) {
		super(queryID, b);	
	}

	public String getSQL(Set<String> complexUserID) {
		String sql = "SELECT ";

		sql += fieldsSQLpart + " FROM " + fromSQLpart;
		sql +=  getConditions(whereSQLpart);

		return sql;
	}

	public String getSQLCount(Set<String> complexUserID) {
		String sql = "SELECT ", fieldsSQLpart = "count(*) ";

		sql += fieldsSQLpart + " FROM " + fromSQLpart;
		sql +=  getConditions(whereSQLpart);
		
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
