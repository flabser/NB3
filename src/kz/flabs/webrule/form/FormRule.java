package kz.flabs.webrule.form;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.RuleUser;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.page.ElementRule;
import kz.flabs.webrule.page.ElementScript;
import kz.lof.appenv.AppEnv;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Deprecated
public class FormRule extends Rule implements Const {
	public boolean isValid;
	public int docType = DOCTYPE_UNKNOWN;
	public String outlineRuleName;
	public boolean isOutlineEnable;
	public HashMap<String, DefaultFieldRule> defaultFieldsMap = new HashMap<String, DefaultFieldRule>();
	public HashMap<String, ShowFieldRule> showFieldsMap = new HashMap<String, ShowFieldRule>();
	public HashMap<String, ISaveField> saveFieldsMap = new HashMap<String, ISaveField>();
	public HashMap<String, SaveFieldRule> saveFieldsMapAccess = new HashMap<String, SaveFieldRule>();
	public HashMap<String, IShowField> signatureFieldsMap = new HashMap<String, IShowField>();
	public SaveFieldRule viewTextSaveRule;

	public boolean qoEnable;
	public ElementScript qoClassName;
	public boolean advancedQSEnable;
	public ElementScript qsClassName;
	public boolean advancedPSEnable;
	public ElementScript psClassName;

	@Deprecated
	public boolean querySaveEnable;
	@Deprecated
	public String querySaveScript;
	@Deprecated
	public Class<GroovyObject> querySaveClass;
	@Deprecated
	public boolean postSaveEnable;
	@Deprecated
	public String postSaveScript;
	@Deprecated
	public Class<GroovyObject> postSaveClass;
	public ArrayList<RuleUser> allowedUsersToShow = new ArrayList<RuleUser>();
	public ArrayList<RuleUser> allowedUsersToSave = new ArrayList<RuleUser>();
	@Deprecated
	public ArrayList<ScriptValue> viewtext = new ArrayList<ScriptValue>();
	public ArrayList<String> viewnumber = new ArrayList<String>();
	public ArrayList<String> viewdate = new ArrayList<String>();
	public ArrayList<ElementRule> elements = new ArrayList<ElementRule>();

	public FormRule(AppEnv env, File ruleFile) throws RuleException {
		super(env, ruleFile);
		try {

			outlineRuleName = XMLUtil.getTextContent(doc, "/rule/outline");
			if (!outlineRuleName.trim().equals("")) {
				isOutlineEnable = true;
			}

			NodeList fields = XMLUtil.getNodeList(doc, "/rule/default/field");
			for (int i = 0; i < fields.getLength(); i++) {
				DefaultFieldRule df = new DefaultFieldRule(fields.item(i), toString());
				if (df.isOn != RunMode.OFF && df.isValid) {
					defaultFieldsMap.put(df.name, df);
				}
				if (df.toSign) {
					signatureFieldsMap.put(df.name, df);
				}
			}

			NodeList defaultActions = XMLUtil.getNodeList(doc, "/rule/default/action");
			for (int i = 0; i < defaultActions.getLength(); i++) {
				FormActionRule df = new FormActionRule(defaultActions.item(i));
				if (df.isOn == RunMode.ON) {
					defaultActionsMap.put(df.type, df);
				}
			}

			NodeList showFields = XMLUtil.getNodeList(doc, "/rule/show/field");
			for (int i = 0; i < showFields.getLength(); i++) {
				ShowFieldRule sf = new ShowFieldRule(showFields.item(i), toString());
				if (sf.isOn != RunMode.OFF && sf.isValid) {
					showFieldsMap.put(sf.name, sf);
				}
				if (sf.toSign) {
					signatureFieldsMap.put(sf.name, sf);
				}
			}

			NodeList showActions = XMLUtil.getNodeList(doc, "/rule/show/action");
			for (int i = 0; i < showActions.getLength(); i++) {
				FormActionRule df = new FormActionRule(showActions.item(i));
				if (df.isOn == RunMode.ON) {
					showActionsMap.put(df.type, df);
				}
			}

			NodeList saveFieldsToShow = XMLUtil.getNodeList(doc, "/rule/show/granted");
			for (int i = 0; i < saveFieldsToShow.getLength(); i++) {
				RuleUser fu = new RuleUser(saveFieldsToShow.item(i), toString());
				if (fu.isOn == RunMode.ON) {
					allowedUsersToShow.add(fu);
				}
			}

			NodeList saveFields = XMLUtil.getNodeList(doc, "/rule/save/field");
			for (int i = 0; i < saveFields.getLength(); i++) {
				SaveFieldRule saveField = new SaveFieldRule(saveFields.item(i));
				if (saveField.isOn && saveField.isValid) {
					if (saveField.type == FieldType.READER || saveField.type == FieldType.AUTHOR) {
						saveFieldsMapAccess.put(saveField.name, saveField);
					} else {
						saveFieldsMap.put(saveField.name, saveField);
					}
				}
			}

			NodeList grantedUsersForSave = XMLUtil.getNodeList(doc, "/rule/save/granted");
			for (int i = 0; i < grantedUsersForSave.getLength(); i++) {
				RuleUser fu = new RuleUser(grantedUsersForSave.item(i), toString());
				if (fu.isOn == RunMode.ON) {
					allowedUsersToSave.add(fu);
				}
			}

			NodeList vtext = XMLUtil.getNodeList(doc, "/rule/viewtext");
			for (int i = 0; i < vtext.getLength(); i++) {
				Node node = vtext.item(i);
				ScriptValue sv = new ScriptValue(node.getTextContent(), "Viewtext of " + toString());
				if (sv.isCompiled) {
					viewtext.add(sv);
				}

			}

			NodeList vnumber = XMLUtil.getNodeList(doc, "/rule/viewnumber");
			for (int i = 0; i < vnumber.getLength(); i++) {
				Node node = vnumber.item(i);
				viewnumber.add(node.getTextContent());
			}

			NodeList vdate = XMLUtil.getNodeList(doc, "/rule/viewdate");
			for (int i = 0; i < vdate.getLength(); i++) {
				Node node = vdate.item(i);
				viewdate.add(node.getTextContent());
			}

			NodeList glossaries = XMLUtil.getNodeList(doc, "/rule/glossary");
			for (int i = 0; i < glossaries.getLength(); i++) {
				Node node = glossaries.item(i);
				GlossaryRule g = new GlossaryRule(node);
				if (g.isOn && g.isValid) {
					addGlossary(g.name, g);
				}
			}

			if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("main")) {
				docType = DOCTYPE_MAIN;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("task")) {
				docType = DOCTYPE_TASK;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("execution")) {
				docType = DOCTYPE_EXECUTION;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("project")) {
				docType = DOCTYPE_PROJECT;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("glossary")) {
				docType = DOCTYPE_GLOSSARY;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("organization")) {
				docType = DOCTYPE_ORGANIZATION;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("department")) {
				docType = DOCTYPE_DEPARTMENT;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("employer")) {
				docType = DOCTYPE_EMPLOYER;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("group")) {
				docType = DOCTYPE_GROUP;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("topic")) {
				docType = DOCTYPE_TOPIC;
			} else if (XMLUtil.getTextContent(doc, "/rule/doctype").equalsIgnoreCase("post")) {
				docType = DOCTYPE_POST;
			}

			Node qoNode = XMLUtil.getNode(doc, "/rule/events/queryopen", false);
			qoClassName = getClassName(qoNode, "queryopen");
			if (qoClassName != null) {
				qoEnable = true;
			}

			Node qsNode = XMLUtil.getNode(doc, "/rule/events/querysave", false);
			qsClassName = getClassName(qsNode, "querysave");
			if (qsClassName != null) {
				advancedQSEnable = true;
			} else {
				String qs = XMLUtil.getTextContent(doc, "/rule/querysave");

			}

			Node psNode = XMLUtil.getNode(doc, "/rule/events/postsave", false);
			psClassName = getClassName(psNode, "postsave");
			if (psClassName != null) {
				advancedPSEnable = true;
			} else {
				String ps = XMLUtil.getTextContent(doc, "/rule/postsave");
				if (!ps.equals("")) {
					postSaveEnable = true;
					postSaveScript = ps;
					ClassLoader parent = getClass().getClassLoader();
					GroovyClassLoader loader = new GroovyClassLoader(parent);
					try {

					} catch (MultipleCompilationErrorsException e) {
						AppEnv.logger.errorLogEntry("PostSaveScript compilation error at form rule compiling=" + id + ":" + e.getMessage());
						postSaveEnable = false;
					}
				}
			}

			NodeList ef = XMLUtil.getNodeList(doc, "/rule/element");
			for (int i = 0; i < ef.getLength(); i++) {
				ElementRule element = new ElementRule(ef.item(i), this);
				if (element.isOn != RunMode.OFF && element.isValid) {
					elements.add(element);
				}
			}

			isValid = true;
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	public String getFieldsToSign() {
		String fieldNames = "<signedfields>";
		for (String field : signatureFieldsMap.keySet()) {
			fieldNames += field + ",";
		}
		if (!fieldNames.equals("")) {
			fieldNames = fieldNames.substring(0, fieldNames.length() - 1);
		}
		fieldNames += "</signedfields>";
		return fieldNames;
	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public String getRuleAsXML(String app) {
		return null;
	}

	@Override
	public void update(Map<String, String[]> fields) throws WebFormValueException {

	}

	private ElementScript getClassName(Node node, String normailzator) {
		ClassLoader parent = getClass().getClassLoader();

		String value = XMLUtil.getTextContent(node, ".", true);
		ValueSourceType qsSourceType = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "@source", true, "STATIC", true));
		try {
			Class<GroovyObject> querySave = null;
			if (qsSourceType == ValueSourceType.GROOVY_FILE || qsSourceType == ValueSourceType.FILE) {
				CompilerConfiguration compiler = new CompilerConfiguration();
				compiler.setTargetDirectory(scriptDirPath);
				GroovyClassLoader loader = new GroovyClassLoader(parent, compiler);
				File groovyFile = new File(scriptDirPath + File.separator + value.replace(".", File.separator) + ".groovy");
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
				if (!value.equals("")) {

				}
			}

		} catch (MultipleCompilationErrorsException e) {
			AppEnv.logger.errorLogEntry("Script compilation error at form rule compiling=" + id + ", node=" + node.getBaseURI());
			AppEnv.logger.errorLogEntry(e.getMessage());
		}
		return null;
	}

}
