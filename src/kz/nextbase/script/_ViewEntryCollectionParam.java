package kz.nextbase.script;

import java.text.SimpleDateFormat;

import kz.lof.scripting._Session;

public class _ViewEntryCollectionParam {

	// private User user;
	private String query;
	private int pageNum = 0;
	private int pageSize = 0;
	private boolean withResponse;
	private boolean withFilter;
	private boolean expandAllResponses;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

	public _ViewEntryCollectionParam(_Session ses) {
		this.pageSize = ses.pageSize;
		// this.user = ses.getUser();
	}

	/*
	 * public _ViewEntryCollectionParam setUser(final User user) { this.user =
	 * user; return this; }
	 * 
	 * public User getUser() { return user; }
	 */

	public String getQuery() {
		return query;
	}

	public _ViewEntryCollectionParam setQuery(String query) {
		this.query = query;
		return this;
	}

	public int getPageNum() {
		return pageNum;
	}

	public _ViewEntryCollectionParam setPageNum(int pageNum) {
		if (pageNum < 0) {
			throw new IllegalArgumentException("incorrect page number: pageNum = " + pageNum + ", pageNum may be >= 0, ([pageNum=0] = last page)");
		}

		this.pageNum = pageNum;
		return this;
	}

	public int getPageSize() {
		return pageSize;
	}

	public _ViewEntryCollectionParam setPageSize(int pageSize) {
		if (pageSize < 0) {
			throw new IllegalArgumentException("incorrect page size: pageSize = " + pageSize + ", pageSize may be >= 0, ([pageSize=0] = no limit)");
		}

		this.pageSize = pageSize;
		return this;
	}

	public boolean withResponse() {
		return withResponse;
	}

	public _ViewEntryCollectionParam setCheckResponse(boolean checkResponse) {
		this.withResponse = checkResponse;
		return this;
	}

	public boolean withFilter() {
		return withFilter;
	}

	public _ViewEntryCollectionParam setUseFilter(boolean useFilter) {
		this.withFilter = useFilter;
		return this;
	}

	public boolean expandAllResponses() {
		return expandAllResponses;
	}

	public _ViewEntryCollectionParam setExpandAllResponses(boolean expandAllResponses) {
		this.expandAllResponses = expandAllResponses;
		return this;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public _ViewEntryCollectionParam setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	@Override
	public String toString() {
		return "queryCondition: " + query + ",\npageNum: " + pageNum + ",\npageSize: " + pageSize + ",\ncheckResponse: " + withResponse
		        + ",\nuseFilter: " + withFilter + ",\nexpandAllResponses: " + expandAllResponses + ",\nSimpleDateFormat: " + dateFormat.toPattern();
	}
}
