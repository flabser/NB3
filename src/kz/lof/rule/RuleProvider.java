package kz.lof.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.webrule.GlobalSetting;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.handler.HandlerRule;
import kz.lof.appenv.AppEnv;
import kz.lof.rule.page.PageRule;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

public class RuleProvider implements Const {
	public GlobalSetting global;

	private HashMap<String, PageRule> pageRuleMap = new HashMap<String, PageRule>();
	private HashMap<String, HandlerRule> handlerRuleMap = new HashMap<String, HandlerRule>();
	private AppEnv env;
	private Element root;

	public RuleProvider(AppEnv env) {
		try {
			// System.out.println("type= " + env.appType);
			this.env = env;
		} catch (Exception ne) {
			AppEnv.logger.errorLogEntry(ne);
		}
	}

	public void initApp(String globalFileName) {
		loadGlobal(globalFileName);
	}

	public Collection<HandlerRule> getHandlerRules(boolean reload) throws RuleException {
		if (reload) {
			handlerRuleMap.clear();
			// loadHandlers();
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

	public boolean resetRules() {
		AppEnv.logger.infoLogEntry("Reload \"" + env.appName + "\" application rules ...");
		pageRuleMap.clear();
		AppEnv.logger.infoLogEntry("Application rules have been reset");
		return true;
	}

	private void loadGlobal(String globalFileName) {
		String globalPath = "rule" + File.separator + env.appName + File.separator + globalFileName;
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
							AppEnv.logger.debugLogEntry("rule " + ruleObj.id + " turn off ");
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

	public PageRule getRule(String id) throws RuleException {
		File docFile = null;
		if (id != null) {
			String ruleID = id.toLowerCase();
			PageRule rule = null;

			if (pageRuleMap.containsKey(ruleID)) {
				rule = pageRuleMap.get(ruleID);
			} else {
				docFile = new File(env.getRulePath() + File.separator + env.appName + File.separator + "Page" + File.separator + ruleID + ".xml");
				rule = new PageRule(env, docFile);
				pageRuleMap.put(ruleID.toLowerCase(), rule);
			}

			rule.plusHit();
			return rule;
		} else {
			return null;
		}

	}
}
