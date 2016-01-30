package kz.flabs.webrule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.page.PageRule;
import kz.flabs.webrule.scheduler.IScheduledProcessRule;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

public class WebRuleProvider implements Const {
	public GlobalSetting global;

	private HashMap<String, PageRule> pageRuleMap = new HashMap<String, PageRule>();
	private HashMap<String, HandlerRule> handlerRuleMap = new HashMap<String, HandlerRule>();
	private ArrayList<IScheduledProcessRule> scheduledRules = new ArrayList<IScheduledProcessRule>();
	private AppEnv env;
	private Element root;

	public WebRuleProvider(AppEnv env) {
		try {
			this.env = env;
		} catch (Exception ne) {
			AppEnv.logger.errorLogEntry(ne);
		}
	}

	public void initApp(String globalFileName) {
		loadGlobal(globalFileName);
	}

	public void loadRules() {
		loadHandlers();
	}

	public IRule getRule(String ruleType, String ruleID) throws RuleException, QueryFormulaParserException {
		File docFile;
		try {
			if (ruleID != null) {
				ruleID = ruleID.toLowerCase();
			}
			IRule rule = null;

			if (ruleType.equalsIgnoreCase("page")) {
				if (pageRuleMap.containsKey(ruleID)) {
					rule = pageRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Page" + File.separator + ruleID + ".xml");
					PageRule pageRule = new PageRule(env, docFile);
					pageRuleMap.put(ruleID.toLowerCase(), pageRule);
					rule = pageRule;
				}
			} else if (ruleType.equalsIgnoreCase("handler")) {
				if (handlerRuleMap.containsKey(ruleID)) {
					rule = handlerRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Handler" + File.separator + ruleID + ".xml");
					HandlerRule handlerRule = new HandlerRule(env, docFile);
					handlerRuleMap.put(ruleID.toLowerCase(), handlerRule);
					rule = handlerRule;
				}
			}
			if (rule != null) {
				rule.plusHit();
			}
			return rule;
		} catch (FileNotFoundException fnf) {
			throw new RuleException("rule \"" + ruleID + "\"(type:" + ruleType + "), not found ");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public IRule getRule(int ruleType, String ruleID) throws RuleException, QueryFormulaParserException {
		File docFile;
		try {
			ruleID = ruleID.toLowerCase();
			IRule rule = null;

			switch (ruleType) {

			case PAGE_RULE:
				if (pageRuleMap.containsKey(ruleID)) {
					rule = pageRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Page" + File.separator + ruleID + ".xml");
					PageRule pageRule = new PageRule(env, docFile);
					pageRuleMap.put(ruleID.toLowerCase(), pageRule);
					rule = pageRule;
				}
				break;
			case HANDLER_RULE:
				if (handlerRuleMap.containsKey(ruleID)) {
					rule = handlerRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Handler" + File.separator + ruleID + ".xml");
					HandlerRule handlerRule = new HandlerRule(env, docFile);
					handlerRuleMap.put(ruleID.toLowerCase(), handlerRule);
					rule = handlerRule;
				}
				break;
			}
			rule.plusHit();
			return rule;
		} catch (FileNotFoundException fnf) {
			throw new RuleException("rule \"" + ruleID + "\"(type:" + ruleType + "), not found ");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public Collection<HandlerRule> getHandlerRules(boolean reload) throws RuleException {
		if (reload) {
			handlerRuleMap.clear();
			loadHandlers();
		}
		return handlerRuleMap.values();

	}

	public Collection<PageRule> getPageRules(boolean reload) throws RuleException {
		if (reload) {
			pageRuleMap.clear();
			loadPages();
		}
		return pageRuleMap.values();

	}

	public boolean resetRule(int ruleType, String ruleID) {
		AppEnv.logger.normalLogEntry("Reset rule \"" + ruleID + "\" from rule pool");
		switch (ruleType) {

		case HANDLER_RULE:
			for (Iterator<Map.Entry<String, HandlerRule>> i = handlerRuleMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry<String, HandlerRule> entry = i.next();
				if (entry.getValue().id.equalsIgnoreCase(ruleID)) {
					i.remove();
					break;
				}
			}
			break;
		}
		return true;
	}

	public boolean resetRules() {
		AppEnv.logger.normalLogEntry("Reload \"" + env.appType + "\" application rules ...");

		pageRuleMap.clear();
		handlerRuleMap.clear();

		loadHandlers();

		AppEnv.logger.normalLogEntry("Application rules have been reloaded");
		return true;
	}

	public ArrayList<IScheduledProcessRule> getScheduledRules() {

		return scheduledRules;

	}

	private void loadGlobal(String globalFileName) {
		String globalPath = "rule" + File.separator + env.appType + File.separator + globalFileName;
		global = new GlobalSetting(globalPath, env);
	}

	private void loadPages() {
		try {
			File docFile = new File(global.rulePath + File.separator + "Page");
			ArrayList<File> fl = getFiles(docFile);
			int n = 0;
			Document doc = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			while (n != fl.size()) {
				File file = null;
				try {
					file = fl.get(n);
					try {
						doc = db.parse(file.toString());
					} catch (SAXParseException e) {
						AppEnv.logger.errorLogEntry("xml file structure error  file=" + file.getAbsolutePath() + ", rule has not loaded");
					}
					root = doc.getDocumentElement();
					String attr = root.getAttribute("type");
					if (attr.equalsIgnoreCase("page")) {
						PageRule ruleObj = new PageRule(env, file);
						if (ruleObj.isOn != RunMode.ON) {
							AppEnv.logger.verboseLogEntry("rule " + ruleObj.id + " turn off ");
						}

						pageRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
					}
				} catch (SAXParseException spe) {
					AppEnv.logger.errorLogEntry("xml file structure error  file=" + file.getAbsolutePath() + ", rule has not loaded");
					AppEnv.logger.errorLogEntry(spe);
				} finally {
					n++;
				}

			}
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	private void loadHandlers() {
		try {
			File docFile = new File(global.rulePath + File.separator + "Handler");
			ArrayList<File> fl = getFiles(docFile);
			int n = 0;
			while (n != fl.size()) {
				File file = null;
				try {
					file = fl.get(n);
					AppEnv.logger.normalLogEntry("Loading handler  rules " + file.getAbsolutePath());
					HandlerRule ruleObj = new HandlerRule(env, file);

					if (ruleObj.isOn != RunMode.ON) {
						AppEnv.logger.verboseLogEntry("rule " + ruleObj.id + " turn off ");
					} else {
						handlerRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
						addScheduledRule(ruleObj);
					}
				} finally {
					n++;
				}

			}
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	private ArrayList<File> getFiles(File docFile) {
		ArrayList<File> fl = new ArrayList<File>();

		if (docFile.isDirectory()) {
			File[] list = docFile.listFiles();
			for (int i = list.length; --i >= 0;) {
				String name = list[i].getName().toLowerCase();
				if (name.endsWith(".xml")) {
					fl.add(list[i]);
				}
			}
		}
		return fl;
	}

	private void addScheduledRule(IScheduledProcessRule rule) {

		if (rule.getTriggerType() == TriggerType.SCHEDULER) {
			scheduledRules.add(rule);
		}
	}
}
