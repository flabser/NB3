package kz.flabs.webrule.page;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Node;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.queries.QueryMacroType;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Caption;
import kz.flabs.webrule.RuleValue;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.query.IQueryRule;
import kz.flabs.webrule.query.QueryFieldRule;
import kz.pchelka.env.Environment;

public class ElementRule implements IQueryRule, Const {
	public ElementType type;
	public boolean isValid = true;
	public String name;
	public boolean hasElementName;
	public RunMode isOn;
	public String value;
	public ElementScript doClassName;
	public QueryType queryType = QueryType.UNKNOWN;
	public RuleValue query;
	public FormulaBlocks queryFormulaBlocks;
	public IQueryFormula queryFormula;

	private QueryMacroType macro;
	private IElement parentRule;

	public ElementRule(Node node, IElement parent) {
		parentRule = parent;
		try {
			name = XMLUtil.getTextContent(node, "name", false);
			if (!name.equals("")) {
				hasElementName = true;
			}
			String mode = XMLUtil.getTextContent(node, "@mode", false);
			if (mode.equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				return;
			}

			type = ElementType.valueOf(XMLUtil.getTextContent(node, "@type", true, "UNKNOWN", false));
			switch (type) {
			case STATIC_TAG:
				value = XMLUtil.getTextContent(node, "value", false);
				break;
			case SCRIPT:
				Node qoNode = XMLUtil.getNode(node, "events/doscript", false);
				doClassName = getClassName(qoNode, "doscript");
				if (doClassName == null) {
					isValid = false;
				}

				break;

			case INCLUDED_PAGE:
				value = XMLUtil.getTextContent(node, "value", false);
				break;
			case QUERY:
				queryType = QueryType.valueOf(XMLUtil.getTextContent(node, "doctype", true, "UNKNOWN", false));
				query = new RuleValue(XMLUtil.getTextContent(node, "query"),
						XMLUtil.getTextContent(node, "query/@source", true, "STATIC", false),
						XMLUtil.getTextContent(node, "query/@type", true, "TEXT", false));
				queryFormulaBlocks = new FormulaBlocks(query.getValue(), queryType);
				if (!queryFormulaBlocks.paramatrizedQuery) {
					queryFormula = parent.getAppEnv().getDataBase().getQueryFormula(parent.getID(), queryFormulaBlocks);
				}
				if (query.getSourceType() == ValueSourceType.MACRO) {
					if (query.getValue().equalsIgnoreCase("RESPONSES")) {
						macro = QueryMacroType.RESPONSES;
					}
				}
				String sortBy = XMLUtil.getTextContent(node, "sortby");
				SortByBlock sbb;
				if (!sortBy.equals("")) {
					sbb = new SortByBlock(sortBy);
				} else {
					sbb = new SortByBlock();
				}
				queryFormulaBlocks.setSortByBlock(sbb);
			default:
				break;
			}

		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
			isValid = false;
		}
	}

	@Override
	public String toString() {
		return "name=\"" + name + "\", value=" + value;
	}

	@SuppressWarnings({ "unchecked", "resource" })
	private ElementScript getClassName(Node node, String normailzator) {
		ClassLoader parent = getClass().getClassLoader();

		String value = XMLUtil.getTextContent(node, ".", true);
		ValueSourceType qsSourceType = ValueSourceType
				.valueOf(XMLUtil.getTextContent(node, "@source", true, "STATIC", true));
		try {
			Class<GroovyObject> querySave = null;
			if (qsSourceType == ValueSourceType.GROOVY_FILE || qsSourceType == ValueSourceType.FILE) {
				CompilerConfiguration compiler = new CompilerConfiguration();
				
				if (Environment.isDevMode){
					compiler.setTargetDirectory("bin");
				}else{
					compiler.setTargetDirectory(parentRule.getScriptDirPath());	
				}
				GroovyClassLoader loader = new GroovyClassLoader(parent, compiler);
				File groovyFile = new File(parentRule.getScriptDirPath() + File.separator
						+ value.replace(".", File.separator) + ".groovy");
				if (groovyFile.exists()) {
					try {
						querySave = loader.parseClass(groovyFile);
						return new ElementScript(qsSourceType, querySave.getName());
					} catch (CompilationFailedException e) {
						AppEnv.logger.errorLogEntry(e);
					} catch (IOException e) {
						AppEnv.logger.errorLogEntry(e);
					}
				} else {
					AppEnv.logger.errorLogEntry("File \"" + groovyFile.getAbsolutePath() + "\" not found");
				}
			} else if (qsSourceType == ValueSourceType.JAVA_CLASS) {
				return new ElementScript(qsSourceType, XMLUtil.getTextContent(node, ".", true));
			} else {
				AppEnv.logger.errorLogEntry("Included script did not implemented, form rule=" + parentRule.getID()
						+ ", node=" + node.getBaseURI());
			}

		} catch (MultipleCompilationErrorsException e) {
			AppEnv.logger.errorLogEntry("Script compilation error at form rule compiling=" + parentRule.getID()
					+ ", node=" + node.getBaseURI());
			AppEnv.logger.errorLogEntry(e.getMessage());
		}
		return null;
	}

	@Override
	public String getID() {
		return parentRule.getID();
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
		return null;
	}

	@Override
	public String getFieldsCondition() {
		return "";
	}

	@Override
	public TagPublicationFormatType getGroupByPublicationFormat() {
		return null;
	}

	@Override
	public RuleValue getQuery() {
		return query;
	}

	@Override
	public RunMode getCacheMode() {
		return RunMode.OFF;
	}

	@Override
	public QueryMacroType getMacro() {
		return macro;
	}

	@Override
	public AppEnv getAppEnv() {
		return parentRule.getAppEnv();
	}

	@Override
	public String getScriptDirPath() {
		return parentRule.getScriptDirPath();
	}

	@Override
	public ArrayList<Caption> getCaptions() {
		return parentRule.getCaptions();
	}

}