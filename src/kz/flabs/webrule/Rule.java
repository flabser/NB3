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

import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ActionType;
import kz.flabs.webrule.constants.RuleType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.form.FormActionRule;
import kz.flabs.webrule.form.GlossaryRule;
import kz.flabs.webrule.scheduler.ScheduleSettings;
import kz.lof.appenv.AppEnv;
import kz.lof.rule.page.ElementRule;
import kz.lof.rule.page.IElement;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class Rule implements IElement, IRule {
	public RunMode isOn = RunMode.ON;
	public boolean isValid = true;
	public String description;
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
	public AppEnv env;
	public ArrayList<ElementRule> elements = new ArrayList<ElementRule>();
	protected org.w3c.dom.Document doc;
	protected RuleType type = RuleType.UNKNOWN;

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
			scriptDirPath = "rule" + File.separator + env.appName + File.separator + "Resources" + File.separator + "scripts";
			id = XMLUtil.getTextContent(doc, "/rule/@id", true);
			if (id.equals("")) {
				id = FilenameUtils.removeExtension(docFile.getName());
			}
			AppEnv.logger.debugLogEntry("Load rule: " + this.getClass().getSimpleName() + ", id=" + id);
			if (XMLUtil.getTextContent(doc, "/rule/@mode").equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				isValid = false;
			}

			if (XMLUtil.getTextContent(doc, "/rule/@anonymous").equalsIgnoreCase("on")) {
				allowAnonymousAccess = true;
			}

			if (XMLUtil.getTextContent(doc, "/rule/@security").equalsIgnoreCase("on")) {
				isSecured = true;
			}

			description = XMLUtil.getTextContent(doc, "/rule/description");

			NodeList captionList = XMLUtil.getNodeList(doc, "/rule/caption");
			for (int i = 0; i < captionList.getLength(); i++) {
				Caption c = new Caption(captionList.item(i));
				if (c.isOn == RunMode.ON) {
					captions.add(c);
				}
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
