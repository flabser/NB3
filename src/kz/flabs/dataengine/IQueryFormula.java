package kz.flabs.dataengine;

import java.util.Set;

import kz.flabs.runtimeobj.Filter;

public interface IQueryFormula {
	String getQueryID();

	String getSQL(Set<String> complexUserID);

	String getSQLCount(Set<String> complexUserID);

	boolean isGroupBy();

	boolean isSortBy();

	String getGroupCondition(String value);

	String getCategoryName();

	String getSQLGroupCount(Set<String> complexUserID);

	Filter getQuickFilter();

	void setQuickFilter(Filter filter);

	String getSQLGroupCount();

	String getSQLCount();

	String getSQL();

	String getSQL(Set<String> complexUserID, int pageSize, int offset);

	String getSQL(Set<String> complexUserID, int pageSize, int offset, String[] filters, String[] sorting);
}
