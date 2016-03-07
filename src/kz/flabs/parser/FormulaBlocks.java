package kz.flabs.parser;

import java.util.regex.Pattern;

import kz.flabs.dataengine.Const;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.webrule.constants.QueryType;

public final class FormulaBlocks implements Const {
	public QueryType docType;
	public boolean paramatrizedQuery;
	public String sql;
	public String sqlCount;
	public boolean isDirectQuery;
	public boolean isGroupBy;
	public boolean isSortBy;
	public String customFieldsTable;
	public boolean hasCustomTable;

	private SortByBlock sortByBlock;
	private Filter quickFilter;

	private static final Pattern blocksPattern = Pattern
	        .compile("\\S+?\\s*(!=|=|<>|<|>|~\\*|~|\\s+match\\s+|\\s+not in\\s+|\\s+in\\s+|\\s+is\\s+){1}\\s*[^\f\n\r\t]+?(\\s*&|\\s*and\\s{1}|\\s*or\\s{1}|\\Z|\\s*\\Z)");
	private static final Pattern sysFieldsPattern = Pattern
	        .compile("^\\(?(form|author|docid|viewtext|viewnumber|regdate|lastupdate|ddbid|parentdocddbid|parentdocid|parentdoctype|has_response)");
	private static final Pattern projectFieldsPattern = Pattern
	        .compile("^\\(?(form|author|docid|viewtext|coordstatus|vnnumber|docversion|isrejected|regdocid)");
	private static final Pattern structureFieldsPattern = Pattern
	        .compile("^\\(?(form|author|docid|viewtext|empid|depid|orgid|bossid|ismain|fullname|shortname|userid|post|isboss|type)");

	public FormulaBlocks(String sql, String sqlCount, QueryType docType) {
		this.docType = docType;
		this.sql = sql;
		this.sqlCount = sqlCount;
		isDirectQuery = true;
	}

	public Pattern getFieldsPattern() {
		switch (docType) {
		case PROJECT:
			return projectFieldsPattern;
		case STRUCTURE:
			return structureFieldsPattern;
		default:
			return sysFieldsPattern;
		}
	}

	public SortByBlock getSortByBlock() {
		return sortByBlock;
	}

	public void setSortByBlock(SortByBlock sortByBlock) {
		this.sortByBlock = sortByBlock;
		isSortBy = true;
	}

}
