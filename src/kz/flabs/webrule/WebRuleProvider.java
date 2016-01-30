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
import kz.flabs.webrule.form.FormRule;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.handler.TriggerType;
import kz.flabs.webrule.outline.OutlineRule;
import kz.flabs.webrule.page.PageRule;
import kz.flabs.webrule.query.QueryRule;
import kz.flabs.webrule.scheduler.IScheduledProcessRule;
import kz.flabs.webrule.view.ViewRule;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

public class WebRuleProvider implements Const {
	public GlobalSetting global;

	private HashMap<String, OutlineRule> outlineRuleMap = new HashMap<String, OutlineRule>();
	private HashMap<String, ViewRule> viewRuleMap = new HashMap<String, ViewRule>();
	private HashMap<String, ViewRule> filterRuleMap = new HashMap<String, ViewRule>();
	private HashMap<String, StaticContentRule> contentRuleMap = new HashMap<String, StaticContentRule>();
	private HashMap<String, FormRule> documentRuleMap = new HashMap<String, FormRule>();
	private HashMap<String, QueryRule> queryRuleMap = new HashMap<String, QueryRule>();
	private HashMap<String, QueryRule> glossariesRuleMap = new HashMap<String, QueryRule>();
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
		AppEnv.logger.normalLogEntry("Loading form rules ...");
		loadForms();
		loadQueries();
		AppEnv.logger.normalLogEntry("Loading handler rules ...");
		loadHandlers();
		loadViews();
		loadFilters();
		AppEnv.logger.normalLogEntry("Rules has loaded");
	}

	public IRule getRule(String ruleType, String ruleID) throws RuleException, QueryFormulaParserException {
		File docFile;
		try {
			if (ruleID != null) {
				ruleID = ruleID.toLowerCase();
			}
			IRule rule = null;

			if (ruleType.equalsIgnoreCase("query") || ruleType.equalsIgnoreCase("count")) {
				if (queryRuleMap.containsKey(ruleID)) {
					rule = queryRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Query" + File.separator + ruleID + ".xml");
					if (docFile.exists()) {
						QueryRule queryRuleObj = new QueryRule(env, docFile);
						queryRuleMap.put(ruleID.toLowerCase(), queryRuleObj);
						rule = queryRuleObj;
						if (queryRuleObj.docTypeAsInt == DOCTYPE_GLOSSARY) {
							glossariesRuleMap.put(ruleID.toLowerCase(), queryRuleObj);
						}
						// rule = (IRule)
						// queryRuleMap.get(ruleID.toLowerCase());
					} else {
						throw new RuleException("Rule \"" + ruleID + "\"(type:" + ruleType + "), has not found");
					}
				}
			} else if (ruleType.equalsIgnoreCase("static")) {
				if (contentRuleMap.containsKey(ruleID)) {
					rule = contentRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Static" + File.separator + ruleID + ".xml");
					StaticContentRule contentRuleObj = new StaticContentRule(env, docFile);
					contentRuleMap.put(ruleID.toLowerCase(), contentRuleObj);
					rule = contentRuleObj;
				}

			} else if (ruleType.equalsIgnoreCase("outline")) {
				if (outlineRuleMap.containsKey(ruleID)) {
					rule = outlineRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Outline" + File.separator + ruleID + ".xml");
					OutlineRule outlineRuleObj = new OutlineRule(env, docFile);
					outlineRuleMap.put(ruleID.toLowerCase(), outlineRuleObj);
					rule = outlineRuleObj;
				}

			} else if (ruleType.equalsIgnoreCase("view") || ruleType.equalsIgnoreCase("filter")) {
				if (viewRuleMap.containsKey(ruleID)) {
					rule = viewRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "View" + File.separator + ruleID + ".xml");
					ViewRule viewRuleObj = new ViewRule(env, docFile);
					viewRuleMap.put(ruleID.toLowerCase(), viewRuleObj);
					rule = viewRuleObj;
				}
			} else if (ruleType.equalsIgnoreCase("filter")) {
				if (filterRuleMap.containsKey(ruleID)) {
					rule = filterRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Filter" + File.separator + ruleID + ".xml");
					ViewRule viewRuleObj = new ViewRule(env, docFile);
					filterRuleMap.put(ruleID.toLowerCase(), viewRuleObj);
					rule = viewRuleObj;
				}
			} else if (ruleType.equalsIgnoreCase("edit") || ruleType.equalsIgnoreCase("save") || ruleType.equalsIgnoreCase("document")
					|| ruleType.equalsIgnoreCase("glossary") || ruleType.equalsIgnoreCase("forum")) {
				if (documentRuleMap.containsKey(ruleID)) {
					rule = documentRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Form" + File.separator + ruleID + ".xml");
					FormRule documentRuleObj = new FormRule(env, docFile);

					documentRuleMap.put(ruleID.toLowerCase(), documentRuleObj);
					rule = documentRuleObj;
				}
			} else if (ruleType.equalsIgnoreCase("page")) {
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
			case QUERY_RULE:
				if (queryRuleMap.containsKey(ruleID)) {
					rule = queryRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Query" + File.separator + ruleID + ".xml");
					if (docFile.exists()) {
						QueryRule queryRuleObj = new QueryRule(env, docFile);
						queryRuleMap.put(ruleID.toLowerCase(), queryRuleObj);
						rule = queryRuleObj;
						if (queryRuleObj.docTypeAsInt == DOCTYPE_GLOSSARY) {
							glossariesRuleMap.put(ruleID.toLowerCase(), queryRuleObj);
						}
						// rule = (IRule)
						// queryRuleMap.get(ruleID.toLowerCase());
					} else {
						throw new RuleException("Rule \"" + ruleID + "\"(type:" + getRuleTypeAsString(ruleType) + "), has not found");
					}
				}
				break;

			case STATICCONTENT_RULE:
				if (contentRuleMap.containsKey(ruleID)) {
					rule = contentRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Static" + File.separator + ruleID + ".xml");
					StaticContentRule contentRuleObj = new StaticContentRule(env, docFile);
					contentRuleMap.put(ruleID.toLowerCase(), contentRuleObj);
					rule = contentRuleObj;
				}
				break;
			case OUTLINE_RULE:
				if (outlineRuleMap.containsKey(ruleID)) {
					rule = outlineRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Outline" + File.separator + ruleID + ".xml");
					OutlineRule outlineRuleObj = new OutlineRule(env, docFile);
					outlineRuleMap.put(ruleID.toLowerCase(), outlineRuleObj);
					rule = outlineRuleObj;
				}
				break;
			case VIEW_RULE:
				if (viewRuleMap.containsKey(ruleID)) {
					rule = viewRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "View" + File.separator + ruleID + ".xml");
					ViewRule viewRuleObj = new ViewRule(env, docFile);
					viewRuleMap.put(ruleID.toLowerCase(), viewRuleObj);
					rule = viewRuleObj;
				}
				break;
			case FILTER_RULE:
				if (filterRuleMap.containsKey(ruleID)) {
					rule = filterRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "View" + File.separator + ruleID + ".xml");
					ViewRule viewRuleObj = new ViewRule(env, docFile);
					filterRuleMap.put(ruleID.toLowerCase(), viewRuleObj);
					rule = viewRuleObj;
				}
				break;
			case FORM_RULE:
				if (documentRuleMap.containsKey(ruleID)) {
					rule = documentRuleMap.get(ruleID);
				} else {
					docFile = new File(global.rulePath + File.separator + "Form" + File.separator + ruleID + ".xml");
					FormRule documentRuleObj = new FormRule(env, docFile);

					documentRuleMap.put(ruleID.toLowerCase(), documentRuleObj);
					rule = documentRuleObj;
				}
				break;
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

	public Collection<ViewRule> getViewRules(boolean reload) throws RuleException {
		if (reload) {
			viewRuleMap.clear();
			loadViews();
		}
		return viewRuleMap.values();
	}

	public Collection<QueryRule> getQueryRules(boolean reload) throws RuleException {
		if (reload) {
			queryRuleMap.clear();
			loadQueries();
		}
		return queryRuleMap.values();

	}

	public Collection<FormRule> getFormRules(boolean reload) throws RuleException {
		if (reload) {
			documentRuleMap.clear();
			loadForms();
		}
		return documentRuleMap.values();

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
		case QUERY_RULE:
			queryRuleMap.remove(ruleID);
			break;
		case STATICCONTENT_RULE:
			contentRuleMap.remove(ruleID);
			break;
		case VIEW_RULE:
			viewRuleMap.remove(ruleID);
			break;
		case FILTER_RULE:
			filterRuleMap.remove(ruleID);
			break;
		case FORM_RULE:
			documentRuleMap.remove(ruleID);
			break;
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
		viewRuleMap.clear();
		filterRuleMap.clear();
		contentRuleMap.clear();
		documentRuleMap.clear();
		queryRuleMap.clear();
		pageRuleMap.clear();
		handlerRuleMap.clear();
		loadForms();
		loadQueries();
		loadHandlers();
		loadViews();
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

	private void loadForms() {
		try {
			File docFile = new File(global.rulePath + File.separator + "Form");
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
						continue;
					}
					AppEnv.logger.normalLogEntry("Loading form rules " + file.getAbsolutePath());
					root = doc.getDocumentElement();
					String attr = root.getAttribute("type");

					if (attr.equals("form")) {

						FormRule ruleObj = new FormRule(env, file);

						if (ruleObj.isOn != RunMode.ON) {
							AppEnv.logger.verboseLogEntry("rule " + ruleObj.id + " turn off ");
						}
						documentRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
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

	private void loadViews() {
		try {
			File docFile = new File(global.rulePath + File.separator + "View");
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
					if (attr.equalsIgnoreCase("view")) {
						ViewRule ruleObj = new ViewRule(env, file);
						if (ruleObj.isOn != RunMode.ON) {
							AppEnv.logger.verboseLogEntry("rule " + ruleObj.id + " turn off ");
						}

						viewRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
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

	private void loadFilters() {
		try {
			File docFile = new File(global.rulePath + File.separator + "Filter");
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
					if (attr.equalsIgnoreCase("filter")) {
						ViewRule ruleObj = new ViewRule(env, file);
						if (ruleObj.isOn != RunMode.ON) {
							AppEnv.logger.verboseLogEntry("rule " + ruleObj.id + " turn off ");
						}

						filterRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
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

	private void loadQueries() {
		try {
			File docFile = new File(global.rulePath + File.separator + "Query");
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
						continue;
					}
					root = doc.getDocumentElement();
					String attr = root.getAttribute("type");

					if (attr.equals("query")) {

						QueryRule ruleObj = new QueryRule(env, file);

						if (ruleObj.isOn != RunMode.ON) {
							AppEnv.logger.verboseLogEntry("rule " + ruleObj.id + " turn off ");
						}

						queryRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
						if (ruleObj.docTypeAsInt == DOCTYPE_GLOSSARY) {
							glossariesRuleMap.put(ruleObj.id.toLowerCase(), ruleObj);
						}
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

	private String getRuleTypeAsString(int ruleType) {
		switch (ruleType) {
		case STATICCONTENT_RULE:
			return "static";
		case VIEW_RULE:
			return "view";
		case FILTER_RULE:
			return "filter";
		case FORM_RULE:
			return "form";
		case QUERY_RULE:
			return "query";
		case PAGE_RULE:
			return "page";
		case HANDLER_RULE:
			return "handler";
		default:
			return "unknown";
		}

	}

	private void addScheduledRule(IScheduledProcessRule rule) {

		if (rule.getTriggerType() == TriggerType.SCHEDULER) {
			scheduledRules.add(rule);
		}
	}
}
