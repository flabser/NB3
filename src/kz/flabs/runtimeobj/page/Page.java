package kz.flabs.runtimeobj.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.scriptprocessor.page.doscript.DoProcessor;
import kz.flabs.servlets.pojo.Outcome;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.flabs.util.XMLResponse;
import kz.flabs.webrule.Caption;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.GlossaryRule;
import kz.flabs.webrule.page.ElementRule;
import kz.flabs.webrule.page.ElementType;
import kz.flabs.webrule.page.PageRule;
import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.IProcessInitiator;

import org.apache.http.HttpStatus;

public class Page implements IProcessInitiator, Const {
	public boolean fileGenerated;
	public boolean toJSON;
	public Outcome outcome;
	public String generatedFilePath;
	public String generatedFileOriginalName;
	public int status = HttpStatus.SC_OK;
	protected AppEnv env;
	protected PageRule rule;

	protected Map<String, String[]> fields = new HashMap<>();
	protected UserSession userSession;

	// private HttpServletRequest request;
	// private HttpServletResponse response;

	/*
	 * public Page(AppEnv env, UserSession userSession, PageRule rule,
	 * HttpServletRequest request, HttpServletResponse response){ this.request =
	 * request; this.response = response; this.userSession = userSession;
	 * this.env = env; this.rule = rule; }
	 */

	public Page(AppEnv env, UserSession userSession, PageRule rule) {
		this.userSession = userSession;
		this.env = env;
		this.rule = rule;
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

	public Map<String, String[]> getFields() {
		return fields;
	}

	public void setFields(Map<String, String[]> fields) {
		this.fields = fields;
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

	public StringBuffer process(Map<String, String[]> formData, String method) throws ClassNotFoundException, RuleException,
	        QueryFormulaParserException, DocumentException, DocumentAccessException, QueryException {
		StringBuffer resultOut = null;
		long start_time = System.currentTimeMillis();
		switch (rule.caching) {
		case NO_CACHING:
			resultOut = getContent(formData, method);
			break;
		case CACHING_IN_USER_SESSION_SCOPE:
			resultOut = userSession.getPage(this, formData);
			break;
		case CACHING_IN_APPLICATION_SCOPE:
			resultOut = env.getPage(this, formData);
			break;
		case CACHING_IN_SERVER_SCOPE:
			resultOut = new Environment().getPage(this, formData);
			break;
		default:
			resultOut = getContent(formData, method);
		}
		DocID toFlash = userSession.getFlashDoc();
		String flashAttr = "";
		if (toFlash != null) {
			flashAttr = "flashdocid=\"" + toFlash.id + "\" flashdoctype=\"" + toFlash.type + "\"";
		}
		StringBuffer output = new StringBuffer(5000);

		output.append("<page id=\"" + rule.id + "\" cache=\"" + rule.caching + "\" elapsed_time = \"" + Util.getTimeDiffInSec(start_time) + "\" "
		        + flashAttr + ">");
		output.append(resultOut);
		return output.append("</page>");
	}

	public void postProcess(Map<String, String[]> formData, String method) throws ClassNotFoundException, RuleException, QueryFormulaParserException,
	        DocumentException, DocumentAccessException, QueryException {
		for (ElementRule elementRule : rule.elements) {
			if (elementRule.type == ElementType.SCRIPT && elementRule.doClassName.getType() == ValueSourceType.JAVA_CLASS) {
				User user = userSession.currentUser;
				DoProcessor sProcessor = new DoProcessor(env, user, userSession.lang, fields, this);
				XMLResponse xmlResp = sProcessor.processJava(elementRule.doClassName.getClassName(), method);
				status = xmlResp.status;
				toJSON = true;
				outcome = xmlResp.json;
				break;
			}
		}

	}

	public String getCacheID() {
		String searchKey = "";
		if (fields != null && fields.containsKey("keyword")) {
			searchKey = fields.get("keyword")[0] != null ? fields.get("keyword")[0] : "";
		}
		return "PAGE_" + env.appType + "_" + rule.id + "_" + userSession.lang + searchKey;

	}

	public StringBuffer getContent(Map<String, String[]> formData, String method) throws ClassNotFoundException, RuleException,
	        QueryFormulaParserException, DocumentException, DocumentAccessException, QueryException {
		fields = formData;

		StringBuffer output = new StringBuffer(1000);
		User user = userSession.currentUser;
		if (rule.runUnderUser.getSourceType() == ValueSourceType.STATIC) {
			user = new User(rule.runUnderUser.value, env);
			user.setSession(userSession);
		}
		if (rule.elements.size() > 0) {
			loop: for (ElementRule elementRule : rule.elements) {
				if (elementRule.hasElementName) {
					output.append("<" + elementRule.name + ">");
				}
				switch (elementRule.type) {
				case SCRIPT:
					XMLResponse xmlResp = null;
					DoProcessor sProcessor = new DoProcessor(env, user, userSession.lang, fields, this);
					switch (elementRule.doClassName.getType()) {
					case GROOVY_FILE:
						xmlResp = sProcessor.processScript(elementRule.doClassName.getClassName());
						break;
					case FILE:
						xmlResp = sProcessor.processScript(elementRule.doClassName.getClassName());
						break;
					case JAVA_CLASS:
						xmlResp = sProcessor.processJava(elementRule.doClassName.getClassName(), method);
						status = xmlResp.status;
						break;
					case UNKNOWN:
						break;
					default:
						break;

					}

					if (xmlResp.type == ResponseType.SHOW_FILE_AFTER_HANDLER_FINISHED) {
						fileGenerated = true;
						generatedFilePath = xmlResp.getMessage("filepath").text;
						generatedFileOriginalName = xmlResp.getMessage("originalname").text;
						break loop;
					} else if (xmlResp.type == ResponseType.JSON) {
						toJSON = true;
						outcome = xmlResp.json;
						break loop;
					} else {
						output.append(xmlResp.toXML());
					}

					break;
				case INCLUDED_PAGE:
					PageRule rule = (PageRule) env.ruleProvider.getRule(PAGE_RULE, elementRule.value);
					// System.out.println(rule.getRuleID());
					IncludedPage page = new IncludedPage(env, userSession, rule);
					output.append(page.process(fields, method));
					break;
				default:
					break;
				}
				if (elementRule.hasElementName) {
					output.append("</" + elementRule.name + ">");
				}
			}
		}
		SourceSupplier captionTextSupplier = new SourceSupplier(env, userSession.lang);
		return output.append(getCaptions(captionTextSupplier, rule.captions));

	}

	protected int[] getParentDocProp(Map<String, String[]> formData) {
		int[] prop = new int[2];
		try {
			prop[0] = Integer.parseInt(formData.get("parentdocid")[0]);
		} catch (Exception nfe) {
			prop[0] = 0;
		}
		try {
			prop[1] = Integer.parseInt(formData.get("parentdoctype")[0]);
		} catch (Exception nfe) {
			prop[1] = DOCTYPE_UNKNOWN;
		}
		return prop;
	}

	@Override
	public String getOwnerID() {
		return rule.getRuleID();
	}

}
