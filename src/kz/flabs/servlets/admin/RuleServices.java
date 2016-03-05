package kz.flabs.servlets.admin;

import java.util.Collection;

import kz.flabs.exception.RuleException;
import kz.flabs.util.Util;
import kz.flabs.webrule.handler.HandlerRule;
import kz.lof.env.Environment;
import kz.lof.rule.page.PageRule;

public class RuleServices {

	String getHandlerRuleList(int pageNum, String app, boolean reload) throws RuleException {
		String xmlFragment = "";
		Collection<HandlerRule> handlerRules = Environment.getApplication(app).ruleProvider.getHandlerRules(reload);

		for (HandlerRule rule : handlerRules) {
			xmlFragment += "<entry mode=\"" + rule.isOn + "\" isvalid=\"" + rule.isValid + "\" docid=\"" + rule.id + "\" doctype=\"queryrule\" "
			        + " parentdocid=\"\" parentdoctype=\"\" author=\"\" regdate=\"" + "\" " + " lastupdated=\""
			        + Util.convertDataTimeToString(rule.lastUpdate) + "\" syncstatus=\"\">";
			xmlFragment += "<rununderuser>" + rule.runUnderUser + "</rununderuser>";
			xmlFragment += "<trigger>" + rule.trigger + "</trigger>";
			xmlFragment += "<app>" + app + "</app>";
			xmlFragment += "</entry>";
		}

		return xmlFragment;
	}

	String getPageRuleList(int pageNum, String app, boolean reload) throws RuleException {
		String xmlFragment = "";
		Collection<PageRule> pageRules = Environment.getApplication(app).ruleProvider.getPageRules(reload);

		return xmlFragment;
	}
}
