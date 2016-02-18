package kz.flabs.runtimeobj.page;

import java.util.ArrayList;
import java.util.HashMap;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.scriptprocessor.page.doscript.DoProcessor;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.util.PageResponse;
import kz.flabs.webrule.Caption;
import kz.flabs.webrule.form.GlossaryRule;
import kz.flabs.webrule.page.ElementRule;
import kz.flabs.webrule.page.PageRule;
import kz.lof.scripting._Session;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script._WebFormData;
import kz.pchelka.scheduler.IProcessInitiator;

import org.apache.http.HttpStatus;

public class Page implements IProcessInitiator, Const {
	public boolean fileGenerated;
	public boolean toJSON;
	public String generatedFilePath;
	public String generatedFileOriginalName;
	public int status = HttpStatus.SC_OK;
	protected AppEnv env;
	protected PageRule rule;
	public PageResponse response;

	protected _WebFormData fields;
	protected UserSession userSession;

	private _Session ses;

	// private HttpServletRequest request;
	// private HttpServletResponse response;

	/*
	 * public Page(AppEnv env, UserSession userSession, PageRule rule,
	 * HttpServletRequest request, HttpServletResponse response){ this.request =
	 * request; this.response = response; this.userSession = userSession;
	 * this.env = env; this.rule = rule; }
	 */

	@Deprecated
	public Page(AppEnv env, UserSession userSession, PageRule rule) {
		this.userSession = userSession;
		this.env = env;
		this.rule = rule;
	}

	public Page(AppEnv env, _Session ses, PageRule pageRule) {
		this.ses = ses;
		this.env = env;
		this.rule = pageRule;
	}

	public String getSpravFieldSet(User user, String lang) throws RuleException, DocumentException, DocumentAccessException,
	        QueryFormulaParserException, QueryException, LocalizatorException {
		StringBuffer glossariesAsText = new StringBuffer("<glossaries>");
		SourceSupplier ss = new SourceSupplier(user, env, lang);
		for (GlossaryRule glos : rule.getGlossary()) {
			glossariesAsText.append("<" + glos.name + ">" + ss.getDataAsXML(glos.valueSource, glos.value, glos.macro, lang) + "</" + glos.name + ">");
		}
		return glossariesAsText.append("</glossaries>").toString();
	}

	public String getCaptions(SourceSupplier captionTextSupplier, ArrayList<Caption> captions) throws DocumentException {
		StringBuffer captionsText = new StringBuffer(100);
		for (Caption cap : captions) {
			captionsText.append("<" + cap.captionID + captionTextSupplier.getValueAsCaption(cap.source, cap.value).toAttrValue() + "></"
			        + cap.captionID + ">");
		}

		if (captionsText.toString().equals("")) {
			return "";
		} else {
			return "<captions>" + captionsText.toString() + "</captions>";
		}

	}

	public String getAsXML(User user, String lang) throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException,
	        QueryException, LocalizatorException {
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);
		String captions = getCaptions(captionTextSupplier, rule.captions);
		String glossarySet = getSpravFieldSet(user, lang);
		return "<content>" + rule.getAsXML() + glossarySet + captions + "</content>";
	}

	public PageOutcome pageProcess(_WebFormData formData, String method) throws ClassNotFoundException, RuleException {
		PageOutcome resultOut = null;
		long start_time = System.currentTimeMillis();
		switch (rule.caching) {
		case NO_CACHING:
			resultOut = getPageContent(formData, method);
			break;
		case CACHING_IN_USER_SESSION_SCOPE:
			// resultOut = userSession.getCachedPage(this, formData);
			break;
		case CACHING_IN_APPLICATION_SCOPE:
			// resultOut = env.getCachedPage(this, formData);
			break;
		case CACHING_IN_SERVER_SCOPE:
			// resultOut = new Environment().getCachedPage(this, formData);
			break;

		default:
			resultOut = getPageContent(formData, method);
		}

		return resultOut;
	}

	public String getCacheID() {
		return "PAGE_" + env.appType + "_" + rule.id + "_" + userSession.lang;

	}

	public PageOutcome getPageContent(_WebFormData webFormData, String method) throws ClassNotFoundException, RuleException {
		fields = webFormData;
		PageOutcome output = new PageOutcome();

		if (rule.elements.size() > 0) {
			for (ElementRule elementRule : rule.elements) {

				switch (elementRule.type) {
				case SCRIPT:
					DoProcessor sProcessor = new DoProcessor(env, ses, fields);
					switch (elementRule.doClassName.getType()) {
					case GROOVY_FILE:
						output = sProcessor.processScenario(elementRule.doClassName.getClassName(), method);
						break;
					case JAVA_CLASS:
						output = sProcessor.processScenario(elementRule.doClassName.getClassName(), method);
						break;
					case UNKNOWN:
						break;
					default:
						break;

					}
					output.setScriptResult(true);
					break;
				case INCLUDED_PAGE:
					PageRule rule = env.ruleProvider.getRule(elementRule.value);
					// System.out.println(rule.getRuleID());
					IncludedPage page = new IncludedPage(env, ses, rule);
					output.addPageOutcome(page.getPageContent(fields, method));
					break;
				default:
					break;
				}
				if (elementRule.hasElementName) {
					output.setName(elementRule.name);
				}
			}

		}

		output.setPageId(rule.id);
		output.setCaptions(getCaptions(rule.captions, ses.getLang()));
		return output;

	}

	@Override
	public String getOwnerID() {
		return rule.getRuleID();
	}

	private HashMap<String, String> getCaptions(ArrayList<Caption> captions, LanguageType lang) {
		HashMap<String, String> translated = new HashMap<String, String>();
		for (Caption cap : captions) {
			translated.put(cap.captionID, env.vocabulary.getWord(cap.captionID, lang));
		}
		return translated;
	}

}
