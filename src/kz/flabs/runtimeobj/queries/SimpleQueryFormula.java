package kz.flabs.runtimeobj.queries;

import java.util.Set;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.Filter;

public class SimpleQueryFormula implements IQueryFormula {

	@Override
	public String getQueryID() {	
		return null;
	}

	@Override
	public String getSQL(Set<String> complexUserID) {
		return null;
	}

	@Override
	public String getSQLCount(Set<String> complexUserID) {
		return null;
	}

	@Override
	public boolean isGroupBy() {
		return false;
	}

	@Override
	public boolean isSortBy() {	
		return false;
	}

	@Override
	public String getGroupCondition(String value) {
		return null;
	}

	@Override
	public String getCategoryName() {
		return null;
	}

	@Override
	public SortByBlock getSortBlock() {
		return null;
	}

	@Override
	public String getSQLGroupCount(Set<String> complexUserID) {
		return null;
	}

	@Override
	public Filter getQuickFilter() {
		return null;
	}

	@Override
	public void setQuickFilter(Filter filter) {
		
	}

	@Override
	public String getSQLGroupCount() {
		return null;
	}

	@Override
	public String getSQLCount() {
		return null;
	}

	@Override
	public String getSQL() {
		return null;
	}

	@Override
	public String getSQL(Set<String> complexUserID, int pageSize, int offset) {
		return null;
	}

	@Override
	public FormulaBlocks getBlocks() {
		return null;
	}

    @Override
    public String getSQL(Set<String> complexUserID, int pageSize, int offset, String[] filters, String[] sorting) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
