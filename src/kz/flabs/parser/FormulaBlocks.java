package kz.flabs.parser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.webrule.constants.QueryType;
import kz.pchelka.log.JavaConsoleLogger;

public final class FormulaBlocks implements Const {
	public ArrayList<Block> blocks = new ArrayList<Block>();
	public QueryType docType;
	public boolean paramatrizedQuery;
	public String sql;
	public String sqlCount;
	public boolean isDirectQuery;
	public boolean isGroupBy;
	public boolean isSortBy;
	public String customFieldsTable;
	public boolean hasCustomTable;

	private GroupByBlock groupByBlock;
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

	public FormulaBlocks(String formula, QueryType docType) {
		this.docType = docType;
		if (docType == QueryType.DOCUMENT || docType == QueryType.GLOSSARY) {
			hasCustomTable = true;
		}
		customFieldsTable = DatabaseUtil.getCustomTableName(docType);
		parse(formula);
	}

	public void parse(String formula) {
		Matcher matcher = blocksPattern.matcher(formula);
		while (matcher.find()) {
			Block block = null;
			String formulaPiece = matcher.group();
			block = new Block(this, formulaPiece);
			blocks.add(block);
			if (block.paramatrized) {
				paramatrizedQuery = true;
			}
		}
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

	public void putParameters(Map<String, String[]> fields) {
		for (Block block : blocks) {
			for (FieldExpression fieldExpr : block.getParamatrizedExpressList().values()) {
				String parValue[] = fields.get(fieldExpr.parameterName);
				if (parValue != null) {
					try {
						fieldExpr.fieldValue = new String(parValue[0].getBytes("ISO-8859-1"), "UTF-8").replace("'", "''");
					} catch (UnsupportedEncodingException e) {
						AppEnv.logger.errorLogEntry(e);
					}
				} else {
					AppEnv.logger.warningLogEntry("Parameter \"" + fieldExpr.parameterName + "\" has not defined");
				}
			}
		}
	}

	public GroupByBlock getGroupByBlock() {
		return groupByBlock;
	}

	public SortByBlock getSortByBlock() {
		return sortByBlock;
	}

	public Filter getQuickFilter() {
		return quickFilter;
	}

	public void setQuickFilter(Filter quickFilter) {
		this.quickFilter = quickFilter;
	}

	public void setGroupByBlock(GroupByBlock groupByBlock) {
		this.groupByBlock = groupByBlock;
		isGroupBy = true;
	}

	public void setSortByBlock(SortByBlock sortByBlock) {
		this.sortByBlock = sortByBlock;
		isSortBy = true;
	}

	public String toXML() {
		String result = "";
		for (Block block : blocks) {
			result += "<block type=\"" + block.blockType + "\">" + block.blockText + "</block>";
		}
		return result;
	}

	public static void main(String[] args) {
		try {
			AppEnv.logger = new JavaConsoleLogger();

			// String formula =
			// "form = 'accrual' or (typeoper = 'operations' or calcstaff#number = 1)";
			// String formula =
			// "form = 'accrual' or (form = 'operations' and calcstaff#number = 1)";
			// String formula = "form = 'demand'";
			// String formula =
			// "form = 'demand' and allcontrol <> 'reset' and status <> 'notActual'";
			// String formula =
			// "form = 'accrual' or (form = 'operations' and calcstaff = '1' )";
			String formula = "vnnumber ~ '$vnnumber' and form ~ '$form' and (docversion > 1 or isrejected = 1)";
			System.out.println(formula + "\n");
			// FormulaBlocks blocks = new
			// FormulaBlocks(formula,DocType.DOCUMENT);
			FormulaBlocks blocks = new FormulaBlocks(formula, QueryType.PROJECT);
			Map<String, String[]> fields = new HashMap<String, String[]>();
			String[] val1 = { "3" };
			String[] val2 = { "incomingprj" };
			fields.put("num", val1);
			fields.put("form", val2);
			blocks.putParameters(fields);

			AppEnv.logger = new JavaConsoleLogger();
			AppEnv env = new AppEnv("Avanti");
			env.setDataBase(new Database(env, false));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
