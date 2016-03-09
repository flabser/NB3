package kz.flabs.webrule.handler;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.xml.Tag;
import kz.flabs.sourcesupplier.DocumentCollectionMacro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.constants.RuleType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.rulefile.RuleFile;
import kz.flabs.webrule.rulefile.RuleTag;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.IScheduledProcessRule;
import kz.flabs.webrule.scheduler.RunUnderUser;
import kz.flabs.webrule.scheduler.ScheduleSettings;
import kz.flabs.webrule.scheduler.ScheduleType;
import kz.lof.appenv.AppEnv;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class HandlerRule extends Rule implements IScheduledProcessRule, Const {
	public RunUnderUser runUnderUser;
	public TriggerType trigger;
	public ToHandle toHandle;
	public DocumentCollectionMacro toHandleMacro;
	public ScheduleSettings scheduleSettings;
	public boolean waitResponse;
	public boolean showFile;
	public String description;
	public Class<GroovyObject> handlerClass;
	public boolean scriptIsValid;
	public ValueSourceType qsSourceType = ValueSourceType.STATIC;
	public String handlerClassName;

	private String script = "";

	public HandlerRule(AppEnv env, File file) throws IOException, RuleException {
		super(env, file);
		try {
			trigger = TriggerType.valueOf(XMLUtil.getTextContent(doc, "/rule/trigger", true, "UNKNOWN", false));
			if (trigger == TriggerType.SCHEDULER) {

				Node schedNode = XMLUtil.getNode(doc, "/rule/scheduler", true);
				if (schedNode != null) {
					scheduleSettings = new ScheduleSettings(file.getAbsolutePath(), schedNode);
				} else {
					AppEnv.logger.errorLogEntry("Schedule in " + id + " rule has not set");
					trigger = TriggerType.UNKNOWN;
					isValid = false;
					return;
				}
			}

			toHandle = new ToHandle(XMLUtil.getNode(doc, "/rule/tohandle", true));

			runUnderUser = new RunUnderUser(XMLUtil.getNode(doc, "/rule/rununderuser", true));
			script = XMLUtil.getTextContent(doc, "/rule/script");

			Node qsNode = XMLUtil.getNode(doc, "/rule/events/trigger", true);
			handlerClassName = getClassName(qsNode);
			if (isOn != RunMode.OFF) {
				if (handlerClassName != null) {
					scriptIsValid = true;
				} else {
					ClassLoader parent = getClass().getClassLoader();
					GroovyClassLoader loader = new GroovyClassLoader(parent);
					try {

					} catch (MultipleCompilationErrorsException e) {
						AppEnv.logger.errorLogEntry("Handler Script compilation error at compiling=" + id + ":" + e.getMessage());
						isValid = false;
					}
				}
			}
			try {
				String mt = XMLUtil.getTextContent(doc, "/rule/waitresponse");
				if (mt.equalsIgnoreCase("true")) {
					waitResponse = true;
				} else {
					waitResponse = false;
				}
			} catch (Exception e) {
				waitResponse = false;
			}

			description = XMLUtil.getTextContent(doc, "rule/description");

			try {
				String mt = XMLUtil.getTextContent(doc, "/rule/showfile");
				if (mt.equalsIgnoreCase("true")) {
					showFile = true;
				} else {
					showFile = false;
				}
			} catch (Exception e) {
				showFile = false;
			}

			type = RuleType.HANDLER;
			isValid = true;
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean save() {
		// filePath
		File fXmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc2;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc2 = dBuilder.parse(fXmlFile);
			doc2.getDocumentElement().normalize();
			Node starttime = doc2.getElementsByTagName("starttime").item(0);
			// starttime.setTextContent(scheduleSettings.getStartTime().getTime()+"");
			// <starttime>Thu Apr 18 14:01:54 ALMT 2013</starttime>
			String minAsText = "00";
			int min = scheduleSettings.getStartTime().get(Calendar.MINUTE);
			if (min < 10) {
				minAsText = "0" + Integer.toString(min);
			} else {
				minAsText = Integer.toString(min);
			}

			starttime.setTextContent(scheduleSettings.getStartTime().get(Calendar.HOUR_OF_DAY) + ":" + minAsText);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc2);
			StreamResult result = new StreamResult(new File(filePath));
			transformer.transform(source, result);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
		// return getAsXML(app).save();
	}

	@Override
	public String getRuleAsXML(String app) {
		String scriptBlock = "";
		if (scriptIsValid) {
			scriptBlock = "<events><trigger source=\"" + qsSourceType + "\">" + handlerClassName + "</trigger></events>";
		} else {
			scriptBlock = "<script><![CDATA[" + script + "]]></script>";
		}
		String xmlText = "<rule id=\"" + id + "\" isvalid=\"" + isValid + "\" app=\"" + app + "\">" + "<description>" + description
		        + "</description>" + "<xsltfile>" + xsltFile + "</xsltfile>" + "<waitresponse>" + waitResponse + "</waitresponse>" + "<trigger>"
		        + trigger + "</trigger>" + scriptBlock + "</rule>";

		return xmlText;
	}

	@Override
	public String toString() {
		return "id=" + id + ", ison=" + isOn + ", schedule=" + scheduleSettings;
	}

	private RuleFile getAsXML(String app) {

		RuleFile rf = new RuleFile(filePath);
		RuleTag ruleTag = rf.addTag("rule");
		ruleTag.setAttr("type", "HANDLER");
		ruleTag.setAttr("id", id);
		ruleTag.setAttr("mode", isOn);
		ruleTag.setAttr("isvalid", isValid);
		ruleTag.setAttr("app", app);
		ruleTag.addTag("description", description);
		ruleTag.addTagWithSource("rununderuser", runUnderUser);
		ruleTag.addTag("trigger", trigger);
		if (scheduleSettings != null) {
			ruleTag.addSchedulerTag(scheduleSettings);
		}
		ruleTag.addTagWithSource("tohandle", toHandle);

		if (scriptIsValid) {
			Tag eTag = ruleTag.addTag("events");
			Tag tTag = eTag.addTag("trigger", handlerClassName);
			tTag.setAttr("source", qsSourceType);
		} else {
			Tag eTag = ruleTag.addTag("events");
			Tag tTag = eTag.addTag("trigger", handlerClassName);
			tTag.setAttr("source", ValueSourceType.UNKNOWN);
			ruleTag.addComment(getSignature(trigger));
			ruleTag.addCDATATag("script", script);
			ruleTag.addComment("}");
		}

		return rf;

	}

	@Override
	public TriggerType getTriggerType() {
		return trigger;
	}

	@Override
	public int hashCode() {
		return script.hashCode();
	}

	public int getCodeHash(String scriptText) {
		return scriptText.hashCode();
	}

	@Override
	public void setScheduleMode(RunMode isOn) {
		scheduleSettings.isOn = isOn;

	}

	@Override
	public RunMode getScheduleMode() {
		if (scheduleSettings != null) {
			return scheduleSettings.isOn;
		} else {
			return RunMode.OFF;
		}
	}

	@Override
	public int getMinuteInterval() {
		return scheduleSettings.minInterval;
	}

	@Override
	public void update(Map<String, String[]> fields) throws WebFormValueException {
		// public DocumentCollectionMacro toHandleMacro;
		// public ScheduleSettings scheduleSettings;
		// public boolean waitResponse;
		// public boolean showFile;
		// public String description;
		// public Class<GroovyObject> handlerClass;

		// runUnderUser = fields.get("rununderuser")[0];
		// trigger = fields.get("trigger")[0];
		// toHandle = fields.get("tohandle")[0];
		setScript(fields.get("script")[0]);
		setDescription(fields.get("description")[0]);
	}

	private static String getSignature(TriggerType trigger) {
		switch (trigger) {
		case PROVIDER:
			return "String doHandler(_Session ses, Map<String, String[]> formData){";
		case SCHEDULER:
			return "String doHandler(_Session session, _DocumentCollection collection){";
		case MANUALLY:
			return "String doHandler(_Session session, _DocumentCollection collection){";
		default:
			return "Unknown trigger";
		}

	}

	private String getClassName(Node node) {
		ClassLoader parent = getClass().getClassLoader();

		String value = XMLUtil.getTextContent(node, ".", true);
		qsSourceType = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "@source", true, "STATIC", true));
		try {
			Class<GroovyObject> querySave = null;
			if (qsSourceType == ValueSourceType.STATIC) {
				if (!value.equals("")) {

				}
			} else if (qsSourceType == ValueSourceType.FILE) {
				CompilerConfiguration compiler = new CompilerConfiguration();
				compiler.setTargetDirectory(scriptDirPath);
				compiler.setClasspath(scriptDirPath);
				GroovyClassLoader loader = new GroovyClassLoader(parent, compiler);
				File groovyFile = new File(scriptDirPath + File.separator + value.replace(".", File.separator) + ".groovy");
				if (groovyFile.exists()) {
					try {
						querySave = loader.parseClass(groovyFile);
						return querySave.getName();
					} catch (CompilationFailedException e) {
						AppEnv.logger.errorLogEntry(e);
					} catch (IOException e) {
						AppEnv.logger.errorLogEntry(e);
					}
				} else {
					AppEnv.logger.errorLogEntry("File \"" + groovyFile.getAbsolutePath() + "\" not found");
				}

			}
		} catch (MultipleCompilationErrorsException e) {
			AppEnv.logger.errorLogEntry("Script compilation error at form rule compiling=" + id + ", node=" + node.getBaseURI());
			AppEnv.logger.errorLogEntry(e.getMessage());
		}
		return null;
	}

	@Override
	public ScheduleType getScheduleType() {
		return scheduleSettings.schedulerType;
	}

	@Override
	public void setNextStartTime(Calendar time) {
		scheduleSettings.setNextStart(time);
		save();
	}

	@Override
	public Calendar getStartTime() {
		return scheduleSettings.getStartTime();
	}

	public String getScript() {
		if (script.contains("doHandler")) {
			return script;
		} else {
			return getSignature(trigger) + script + "}";
		}
	}

	@Override
	public String getClassName() {
		return handlerClassName;
	}

	@Override
	public boolean scriptIsValid() {
		return scriptIsValid;
	}

	@Override
	public ArrayList<DaysOfWeek> getDaysOfWeek() {
		return scheduleSettings.daysOfWeek;

	}

	@Override
	public String getProcessID() {
		// TODO Auto-generated method stub
		return null;
	}
}
