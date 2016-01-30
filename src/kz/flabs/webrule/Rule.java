package kz.flabs.webrule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.exception.WebFormValueExceptionType;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ActionType;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.RuleType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.FormActionRule;
import kz.flabs.webrule.form.GlossaryRule;
import kz.flabs.webrule.page.ElementRule;
import kz.flabs.webrule.page.IElement;
import kz.flabs.webrule.scheduler.ScheduleSettings;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class Rule implements IElement, IRule {
	public RunMode isOn = RunMode.ON;
	public boolean isValid = true;
	public String description;
	public RuleValue runUnderUser;
	public String id = "unknown";
	public String xsltFile;
	public PublishAsType publishAs = PublishAsType.XML;
	public String app;
	public Date lastUpdate = new Date();
	public Date regDate = new Date();
	public String filePath;
	public String parentDirPath;
	public String scriptDirPath;
	public int hits;
	public ArrayList<Caption> captions = new ArrayList<Caption>();
	public HashMap<ActionType, FormActionRule> defaultActionsMap = new HashMap<ActionType, FormActionRule>();
	public HashMap<ActionType, FormActionRule> showActionsMap = new HashMap<ActionType, FormActionRule>();
	public ScheduleSettings scheduleSettings;
	public boolean isSecured;
	public QueryType docType = QueryType.UNKNOWN;
	public boolean addToHistory;
	public AppEnv env;
	public ArrayList<ElementRule> elements = new ArrayList<ElementRule>();
	protected org.w3c.dom.Document doc;
	protected RuleType type = RuleType.UNKNOWN;

	private Title title;
	private boolean allowAnonymousAccess;
	private HashMap<String, GlossaryRule> glossary = new HashMap<String, GlossaryRule>();

	protected Rule(AppEnv env, File docFile) throws RuleException {
		try {
			this.env = env;
			DocumentBuilderFactory pageFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder pageBuilder = pageFactory.newDocumentBuilder();
			Document xmlFileDoc = pageBuilder.parse(docFile.toString());
			doc = xmlFileDoc;
			filePath = docFile.getAbsolutePath();
			parentDirPath = docFile.getParentFile().getAbsolutePath();
			scriptDirPath = env.globalSetting.rulePath + File.separator + "Resources" + File.separator + "scripts";
			id = XMLUtil.getTextContent(doc, "/rule/@id", true);
			if (id.equals("")) {
				id = FilenameUtils.removeExtension(docFile.getName());
			}
			AppEnv.logger.verboseLogEntry("Load rule: " + this.getClass().getSimpleName() + ", id=" + id);
			if (XMLUtil.getTextContent(doc, "/rule/@mode").equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				isValid = false;
			}

			if (XMLUtil.getTextContent(doc, "/rule/@anonymous").equalsIgnoreCase("on")) {
				allowAnonymousAccess = true;
			}

			if (XMLUtil.getTextContent(doc, "/rule/@history").equalsIgnoreCase("on")) {
				addToHistory = true;
			}

			if (XMLUtil.getTextContent(doc, "/rule/@security").equalsIgnoreCase("on")) {
				isSecured = true;
			}

			xsltFile = XMLUtil.getTextContent(doc, "/rule/xsltfile");
			if (!xsltFile.equals("")) {
				publishAs = PublishAsType.HTML;
			}
			description = XMLUtil.getTextContent(doc, "/rule/description");

			setRunUnderUser(XMLUtil.getTextContent(doc, "/rule/rununderuser", false, Macro.CURRENT_USER.toString(), false),
					XMLUtil.getTextContent(doc, "/rule/rununderuser/@source", true, "MACRO", false),
					XMLUtil.getTextContent(doc, "/rule/rununderuser/@type", true, "TEXT", false));

			NodeList captionList = XMLUtil.getNodeList(doc, "/rule/caption");
			for (int i = 0; i < captionList.getLength(); i++) {
				Caption c = new Caption(captionList.item(i));
				if (c.isOn == RunMode.ON) {
					captions.add(c);
				}
			}

			NodeList titleList = XMLUtil.getNodeList(doc, "/rule/title");
			if (titleList.getLength() > 0) {
				title = new Title(titleList.item(0));
			} else {
				title = new Title(description);
			}

		} catch (SAXParseException spe) {
			AppEnv.logger.errorLogEntry("XML-file structure error (" + docFile.getAbsolutePath() + ")");
			AppEnv.logger.errorLogEntry(spe);
		} catch (FileNotFoundException e) {
			throw new RuleException("Rule \"" + docFile.getAbsolutePath() + "\" has not found");
		} catch (ParserConfigurationException e) {
			AppEnv.logger.errorLogEntry(e);
		} catch (IOException e) {
			AppEnv.logger.errorLogEntry(e);
		} catch (SAXException se) {
			AppEnv.logger.errorLogEntry(se);
		}

	}

	protected String[] getWebFormValue(String fieldName, Map<String, String[]> fields) throws WebFormValueException {
		try {
			return fields.get(fieldName);
		} catch (Exception e) {
			throw new WebFormValueException(WebFormValueExceptionType.FORMDATA_INCORRECT, fieldName);
		}
	}

	protected void setIsOn(String isOnAsText) {
		if (isOnAsText.equalsIgnoreCase("on")) {
			isOn = RunMode.ON;
		} else {
			isOn = RunMode.OFF;
		}
	}

	protected void setDescription(String d) {
		description = d;
	}

	protected void setID(String id) {
		this.id = id;
	}

	protected void setCaptions(String[] id) {

	}

	protected void setRunUnderUser(String value, String st, String ft) {
		runUnderUser = new RuleValue(value, st, ft);
	}

	protected String getRunUnderUserSource() {
		String value = "<rununderusersource><query>";
		value += "<entry viewtext=\"" + ValueSourceType.STATIC + "\"></entry>";
		value += "<entry viewtext=\"" + ValueSourceType.MACRO + "\"></entry>";
		value += "</query></rununderusersource>";
		if (runUnderUser.getSourceType() == ValueSourceType.MACRO) {
			value += getRunUnderUserAvailableMacro();
		}
		return value;
	}

	private String getRunUnderUserAvailableMacro() {
		String value = "<rununderusermacro><query>";
		value += "<entry viewtext=\"" + Macro.CURRENT_USER + "\"></entry>";
		value += "</query></rununderusermacro>";
		return value;
	}

	public Collection<GlossaryRule> getGlossary() {
		return glossary.values();
	}

	public void addGlossary(String lang, GlossaryRule glos) {
		glossary.put(lang, glos);
	}

	@Override
	public void plusHit() {
		hits++;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " id=" + id + ", file=" + filePath;
	}

	public String getXSLT() {
		return xsltFile.replace("\\", File.separator);
	}

	public String getAsXML() {
		return "";
	}

	@Override
	public String getRuleAsXML(String app) {
		String xmlText = "<rule id=\"" + id + "\" isvalid=\"" + isValid + "\" app=\"" + app + "\" ison=\"" + isOn + "\">" + "<description>"
				+ description + "</description>";
		return xmlText + "</fields></rule>";
	}

	@Override
	public Title getTitle() {
		return title;
	}

	@Override
	public boolean addToHistory() {
		return addToHistory;
	}

	@Override
	abstract public void update(Map<String, String[]> fields) throws WebFormValueException;

	@Override
	abstract public boolean save();

	@Override
	public boolean isAnonymousAccessAllowed() {
		return allowAnonymousAccess;
	}

	@Override
	public String getRuleID() {
		return type + "_" + id;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public AppEnv getAppEnv() {
		return env;
	}

	@Override
	public String getScriptDirPath() {
		return scriptDirPath;
	}

	@Override
	public ArrayList<Caption> getCaptions() {
		return captions;
	}

}
