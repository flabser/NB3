package kz.flabs.webrule.query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.GroupByBlock;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.queries.FieldValueMacroType;
import kz.flabs.runtimeobj.queries.Query;
import kz.flabs.runtimeobj.queries.QueryFactory;
import kz.flabs.runtimeobj.queries.QueryMacroType;
import kz.flabs.runtimeobj.xml.Tag;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.RuleUser;
import kz.flabs.webrule.RuleValue;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.rulefile.RuleFile;
import kz.flabs.webrule.rulefile.RuleTag;
import kz.flabs.webrule.scheduler.RunUnderUser;
import kz.pchelka.log.JavaConsoleLogger;

import org.w3c.dom.NodeList;

@Deprecated
public class QueryRule extends Rule implements IQueryRule, Const {
	public int docTypeAsInt = DOCTYPE_UNKNOWN;

	public String customFieldsTable = "CUSTOM_FIELDS";
	public String groupBy;
	public String sortBy;
	public TagPublicationFormatType groupByPublicationFormat;
	public TagPublicationFormatType sortByPublicationFormat;
	public String form;

	public QueryFieldRule fields[];
	public ArrayList<QueryFieldRule> fieldsList = new ArrayList<QueryFieldRule>();
	public HashMap<String, QueryFieldRule> fieldsMap = new HashMap<String, QueryFieldRule>();
	public boolean turboMode;
	public RunMode cacheMode = RunMode.OFF;
	public RunUnderUser runUnderUser;

	private String fieldsCondition = "";
	private FormulaBlocks queryFormulaBlocks;
	private IQueryFormula queryFormula;
	private QueryType queryType = QueryType.UNKNOWN;
	private RuleValue query;
	private QueryMacroType macro;

	public QueryRule(AppEnv env, File ruleFile) throws QueryFormulaParserException, RuleException {
		super(env, ruleFile);

		query = new RuleValue(XMLUtil.getTextContent(doc, "/rule/query"), XMLUtil.getTextContent(doc, "/rule/query/@source", true, "STATIC", false),
		        XMLUtil.getTextContent(doc, "/rule/query/@type", true, "TEXT", false));

		NodeList saveFieldsToShow = XMLUtil.getNodeList(doc, "/rule/granted");
		for (int i = 0; i < saveFieldsToShow.getLength(); i++) {
			RuleUser fu = new RuleUser(saveFieldsToShow.item(i), toString());
			if (fu.isOn == RunMode.ON) {
				// allowedUsersToShow.add(fu);
			}
		}

		runUnderUser = new RunUnderUser(XMLUtil.getNode(doc, "/rule/rununderuser", true));

		if (query.getSourceType() == ValueSourceType.MACRO) {
			if (query.getValue().equalsIgnoreCase("RESPONSES")) {
				macro = QueryMacroType.RESPONSES;
			}
		}
		setDocType(XMLUtil.getTextContent(doc, "/rule/doctype", true, "UNKNOWN", false));

		form = XMLUtil.getTextContent(doc, "/rule/doctype/@form");

		String sql = XMLUtil.getTextContent(doc, "/rule/sql");
		if (!sql.equals("")) {
			queryFormulaBlocks = new FormulaBlocks(sql, XMLUtil.getTextContent(doc, "/rule/sqlcount"), queryType);
			queryFormula = env.getDataBase().getQueryFormula(id, queryFormulaBlocks);
		} else {
			queryFormulaBlocks = new FormulaBlocks(query.getValue(), queryType);
			if (!queryFormulaBlocks.paramatrizedQuery) {
				queryFormula = env.getDataBase().getQueryFormula(id, queryFormulaBlocks);
			}
		}

		groupBy = XMLUtil.getTextContent(doc, "rule/groupby");
		groupByPublicationFormat = TagPublicationFormatType.valueOf(XMLUtil.getTextContent(doc, "rule/groupby/@publishas", true, "AS_IS", false));
		if (!groupBy.equals("")) {
			GroupByBlock gbb = new GroupByBlock(groupBy, queryType, groupByPublicationFormat);
			queryFormulaBlocks.setGroupByBlock(gbb);
		}

		sortBy = XMLUtil.getTextContent(doc, "rule/sortby");
		sortByPublicationFormat = TagPublicationFormatType.valueOf(XMLUtil.getTextContent(doc, "rule/sortby/@publishas", true, "AS_IS", false));

		SortByBlock sbb;
		if (!sortBy.equals("")) {
			sbb = new SortByBlock(sortBy);
		} else {
			sbb = new SortByBlock();
		}
		queryFormulaBlocks.setSortByBlock(sbb);

		org.w3c.dom.Element root = doc.getDocumentElement();
		NodeList nodename = root.getElementsByTagName("field");
		turboMode = true;
		for (int i = 0; i < nodename.getLength(); i++) {
			QueryFieldRule queryField = new QueryFieldRule(nodename.item(i));
			if (queryField.isOn == RunMode.ON) {
				fieldsList.add(queryField);
				fieldsMap.put(queryField.name, queryField);
				if (queryField.valueSource != ValueSourceType.RESULTSET) {
					turboMode = false;
				}
			}
		}

		if (XMLUtil.getTextContent(doc, "/rule/cache/@mode").equalsIgnoreCase("ON")) {
			cacheMode = RunMode.ON;
		} else {
			if (queryType == QueryType.GLOSSARY && !(XMLUtil.getTextContent(doc, "/rule/cache/@mode").equalsIgnoreCase("OFF"))) {
				cacheMode = RunMode.ON;
			}
		}

		if (turboMode) {
			String c = "";
			if (fieldsList.size() > 0) {
				for (QueryFieldRule field : fieldsList) {
					if (!c.equals("")) {
						c += " or ";
					}
					c += customFieldsTable + ".NAME = '" + field.value + "'";
				}
				fieldsCondition = " and (" + c + ")";
			}
		}

		fields = new QueryFieldRule[fieldsList.size()];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = fieldsList.get(i);

		}
	}

	@Override
	public void update(Map<String, String[]> fields) throws WebFormValueException {
		setID(getWebFormValue("id", fields)[0]);
		setIsOn(getWebFormValue("ison", fields)[0]);
		setDescription(getWebFormValue("description", fields)[0]);
		setCaptions(getWebFormValue("captions", fields));
		setDocType(getWebFormValue("doctype", fields)[0]);
		setRunUnderUser(getWebFormValue("rununderuser", fields)[0]);
		setCacheMode(getWebFormValue("cachemode", fields)[0]);
		setQuery(getWebFormValue("query", fields)[0]);
		setGroupBy(getWebFormValue("groupby", fields)[0]);
		setGroupByPublicationFormat(getWebFormValue("groupbypubformat", fields)[0]);
		setFieldsCondition(getWebFormValue("fieldscondition", fields)[0]);
		setFields(getWebFormValue("fields", fields));
	}

	private void setDocType(String string) {
		try {
			queryType = QueryType.valueOf(string);
			switch (queryType) {
			case DOCUMENT:
				docTypeAsInt = DOCTYPE_MAIN;
				break;
			case TASK:
				docTypeAsInt = DOCTYPE_TASK;
				break;
			case EXECUTION:
				docTypeAsInt = DOCTYPE_EXECUTION;
				break;
			case PROJECT:
				docTypeAsInt = DOCTYPE_PROJECT;
				break;
			case GLOSSARY:
				docTypeAsInt = DOCTYPE_GLOSSARY;
				customFieldsTable = "CUSTOM_FIELDS_GLOSSARY";
				break;
			case STRUCTURE:
				docTypeAsInt = DOCTYPE_UNKNOWN;
				break;
			case GROUP:
				docTypeAsInt = DOCTYPE_GROUP;
				break;
			case FORUM_THREAD:
				docTypeAsInt = DOCTYPE_UNKNOWN;
				break;
			default:
				docTypeAsInt = DOCTYPE_MAIN;
			}
		} catch (Exception e) {
			docTypeAsInt = DOCTYPE_UNKNOWN;
		}
	}

	private void setRunUnderUser(String string) {
		setRunUnderUser(string, "static", "userid");
	}

	private void setCacheMode(String modeAsText) {
		if (modeAsText.equalsIgnoreCase("on")) {
			cacheMode = RunMode.ON;
		} else {
			cacheMode = RunMode.OFF;
		}
	}

	private void setQuery(String string) {

	}

	private void setGroupBy(String string) {

	}

	private void setGroupByPublicationFormat(String string) {

	}

	private void setFieldsCondition(String string) {

	}

	private void setFields(String[] string) {

	}

	@Override
	public boolean save() {
		return getAsXML(app).save();
	}

	@Override
	public String getRuleAsXML(String app) {
		String xmlText = "<rule id=\"" + id + "\" isvalid=\"" + isValid + "\" app=\"" + app + "\" ison=\"" + isOn + "\">" + "<description>"
		        + description + "</description>" + "<doctype>" + queryType + "</doctype>" + "<hits>" + hits + "</hits>" + "<turbomode>" + turboMode
		        + "</turbomode>" + "<rununderuser>" + runUnderUser.getValue() + "</rununderuser>" + "<cachemode>" + cacheMode + "</cachemode>"
		        + "<query>" + query.toXML() + "</query>" + "<queryformulablocks>" + queryFormulaBlocks.toXML() + "</queryformulablocks>"
		        + "<groupby>" + groupBy + "</groupby>" + "<groupbypublicationformat>" + groupByPublicationFormat + "</groupbypublicationformat>"
		        + "<sortby>" + sortBy + "</sortby>" + "<sortbypublicationformat>" + sortByPublicationFormat + "</sortbypublicationformat>"
		        + "<fieldscondition>" + fieldsCondition + "</fieldscondition><fields>";
		for (QueryFieldRule field : fieldsList) {
			xmlText += "<field>" + field.toXML() + "</field>";
		}
		return xmlText + "</fields>" + getGlossaries() + "</rule>";

	}

	private String getGlossaries() {
		StringBuffer value = new StringBuffer(100);
		value.append("<glossaries>");
		value.append(getRunUnderUserSource());
		value.append("<doctypes><query>");
		value.append("<entry viewtext=\"" + QueryType.DOCUMENT + "\"></entry>");
		value.append("<entry viewtext=\"" + QueryType.TASK + "\"></entry>");
		value.append("<entry viewtext=\"" + QueryType.EXECUTION + "\"></entry>");
		value.append("<entry viewtext=\"" + QueryType.PROJECT + "\"></entry>");
		value.append("<entry viewtext=\"" + QueryType.STRUCTURE + "\"></entry>");
		value.append("<entry viewtext=\"" + QueryType.GLOSSARY + "\"></entry>");
		value.append("<entry viewtext=\"" + QueryType.ROLE + "\"></entry>");
		value.append("</query></doctypes>");
		value.append("<querysources><query>");
		value.append("<entry viewtext=\"" + ValueSourceType.STATIC + "\"></entry>");
		value.append("<entry viewtext=\"" + ValueSourceType.MACRO + "\"></entry>");
		value.append("</query></querysources>");
		if (query.getSourceType() == ValueSourceType.MACRO) {
			value.append("<querymacros><query>");
			switch (queryType) {
			case DOCUMENT:
				value.append("<entry viewtext=\"" + QueryMacroType.RESPONSES + "\"></entry>");
				break;
			case TASK:
				value.append("<entry viewtext=\"" + QueryMacroType.TASKSFORME + "\"></entry>");
				value.append("<entry viewtext=\"" + QueryMacroType.MYTASKS + "\"></entry>");
				value.append("<entry viewtext=\"" + QueryMacroType.COMPLETETASKS + "\"></entry>");
				break;
			case EXECUTION:
				break;
			case PROJECT:
				value.append("<entry viewtext=\"" + QueryMacroType.WAITFORCOORD + "\"></entry>");
				value.append("<entry viewtext=\"" + QueryMacroType.WAITFORSIGN + "\"></entry>");
				break;
			case GLOSSARY:
				break;
			case STRUCTURE:
				value.append("<entry viewtext=\"" + QueryMacroType.RESPONSES + "\"></entry>");
				value.append("<entry viewtext=\"" + QueryMacroType.STRUCTURE + "\"></entry>");
				value.append("<entry viewtext=\"" + QueryMacroType.EXPANDED_STRUCTURE + "\"></entry>");
				break;
			case ROLE:
			}
			value.append("</query></querymacros>");
		}
		if (!groupBy.equals("")) {
			value.append("<publicationformats><query>");
			value.append("<entry viewtext=\"" + TagPublicationFormatType.AS_IS + "\"></entry>");
			value.append("<entry viewtext=\"" + TagPublicationFormatType.EMPLOYER + "\"></entry>");
			value.append("<entry viewtext=\"" + TagPublicationFormatType.DEPARTMENT + "\"></entry>");
			value.append("</query></publicationformats>");
		}
		value.append("<fieldsources><query>");
		value.append("<entry viewtext=\"" + FieldValueMacroType.RESULTSET + "\"></entry>");
		value.append("</query></fieldsources>");
		value.append("</glossaries>");
		return value.toString();
	}

	private RuleFile getAsXML(String app) {
		RuleFile rf = new RuleFile(filePath);
		RuleTag ruleTag = rf.addTag("rule");
		ruleTag.setAttr("type", "QUERY");
		ruleTag.setAttr("id", id);
		ruleTag.setAttr("mode", isOn);
		ruleTag.setAttr("isvalid", isValid);
		ruleTag.setAttr("app", app);
		ruleTag.addTag("description", description);
		ruleTag.addTagWithSource("rununderuser", runUnderUser);
		Tag docTypeTag = ruleTag.addTag("doctype", queryType);
		docTypeTag.setAttr("form", form);
		ruleTag.addTagWithSource("query", query);

		for (QueryFieldRule field : fieldsList) {
			RuleTag fieldTag = (RuleTag) ruleTag.addTag("field");
			fieldTag.setAttr("mode", field.isOn);
			fieldTag.addTagWithSource("value", field);
		}
		return rf;
	}

	public static void main(String[] args) {
		try {
			Set<DocID> toExpandResponses = new HashSet<DocID>();
			StringBuffer xmlContent = new StringBuffer(5000);
			File ruleFile = new File("C:" + File.separator + "workspace" + File.separator + "NextBase" + File.separator + "rule" + File.separator
			        + "Avanti" + File.separator + "Query" + File.separator + "corresp.xml");
			AppEnv.logger = new JavaConsoleLogger();
			AppEnv env = new AppEnv("Avanti");
			env.setDataBase(new Database(env, false));
			// env.ruleProvider.loadRules();
			QueryRule ruleObj = new QueryRule(env, ruleFile);
			ISystemDatabase sysDb = DatabaseFactory.getSysDatabase();
			Query query = QueryFactory.getQuery(env, ruleObj, sysDb.getUser(Const.sysUser));
			int result = query.fetch(1, 1000, 0, 0, toExpandResponses, null, null, null);
			if (result > -1) {
				xmlContent.append(query.toXML());
			}

			System.out.println(xmlContent);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "type=QueryRule, id=" + id + ", docType=" + queryType + ", " + query;
	}

	@Override
	public FormulaBlocks getQueryFormulaBlocks() {
		return queryFormulaBlocks;
	}

	@Override
	public IQueryFormula getQueryFormula() {
		return queryFormula;
	}

	@Override
	public QueryType getQueryType() {
		return queryType;
	}

	@Override
	public QueryFieldRule[] getFields() {
		return fields;
	}

	@Override
	public String getFieldsCondition() {
		return fieldsCondition;
	}

	@Override
	public TagPublicationFormatType getGroupByPublicationFormat() {
		return groupByPublicationFormat;
	}

	@Override
	public RuleValue getQuery() {
		return query;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public RunMode getCacheMode() {
		return cacheMode;
	}

	@Override
	public QueryMacroType getMacro() {
		return macro;
	}

}
