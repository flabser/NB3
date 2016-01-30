package kz.flabs.servlets.admin;

import java.util.Collection;
import kz.pchelka.env.Environment;
import kz.flabs.exception.RuleException;
import kz.flabs.util.Util;
import kz.flabs.webrule.form.FormRule;
import kz.flabs.webrule.handler.HandlerRule;
import kz.flabs.webrule.page.PageRule;

public class RuleServices {	
		
	
	String getFormRuleList(int pageNum, String app, boolean reload) throws RuleException {
		String xmlFragment = "";		
		int startEntry = Environment.getApplication(app).getDataBase().calcStartEntry(pageNum, AdminProvider.pageSize);
		Collection<FormRule> queryRules = Environment.getApplication(app).ruleProvider.getFormRules(reload);
		FormRule[] ruleArray = new FormRule[queryRules.size()];
		ruleArray = (FormRule[]) queryRules.toArray(ruleArray);
		for(int i = startEntry; i < ruleArray.length; i++ ){
			FormRule rule = ruleArray[i];
			xmlFragment += "<entry docid=\"" + rule.id +"\" doctype=\"formrule\" " +
			" parentdocid=\"\" parentdoctype=\"\" author=\"\" regdate=\"" + Util.convertDataTimeToString(rule.regDate)  +"\" " +
					" lastupdated=\"" + Util.convertDataTimeToString(rule.lastUpdate) + "\">";
				xmlFragment += "<mode>" + rule.isOn + "</mode>";
				xmlFragment += "<doctype>" + rule.docType + "</doctype>";
				xmlFragment += "<hits>" + rule.hits + "</hits>";
				xmlFragment += "<app>" + app + "</app>";
				xmlFragment += "</entry>";
		}
		
	
		
		return xmlFragment;
	}
	
	String getHandlerRuleList(int pageNum, String app, boolean reload) throws RuleException {
		String xmlFragment = "";
		Collection<HandlerRule> handlerRules = Environment.getApplication(app).ruleProvider.getHandlerRules(reload);
		
		for(HandlerRule rule: handlerRules){
			xmlFragment += "<entry mode=\"" + rule.isOn + "\" isvalid=\"" + rule.isValid + "\" docid=\"" + rule.id +"\" doctype=\"queryrule\" " +
			" parentdocid=\"\" parentdoctype=\"\" author=\"\" regdate=\""  +"\" " +
					" lastupdated=\"" + Util.convertDataTimeToString(rule.lastUpdate) + "\" syncstatus=\"\">";
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
		
		for(PageRule rule: pageRules){
				xmlFragment += "<entry  id=\"" + rule.id + "\" url=\"Provider?type=edit&amp;element=page_rule&amp;id=" + rule.id + "&amp;app=" + app +"\" >" +
						"<ison>" + rule.isOn + "</ison>" +
						"<rununderuser>" + rule.runUnderUser + "</rununderuser>" +
						"<cache>" + rule.caching + "</cache>" +
						"<elements>" + rule.elements + "</elements>" +
						"<hits>" + rule.hits + "</hits>" +
						"<app>" + app + "</app></entry>";
		}
		
	
		return xmlFragment;
	}
}
