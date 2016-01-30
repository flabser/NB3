package kz.flabs.runtimeobj;

import java.util.*;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.*;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.outline.Outline;
import kz.flabs.runtimeobj.queries.Query;
import kz.flabs.runtimeobj.queries.QueryFactory;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.*;
import kz.flabs.webrule.outline.OutlineRule;
import kz.flabs.webrule.query.QueryRule;
import kz.flabs.webrule.view.ViewColumnRule;
import kz.flabs.webrule.view.ViewRule;

public class View extends Content implements Const {
	private ViewRule viewRule;
	private UserSession userSession;
	private SourceSupplier captionTextSupplier;
	private AppEnv env;
	private String lang;
	private Outline outline;

	public View(AppEnv env, Rule r, UserSession userSession, String lang) throws RuleException, QueryFormulaParserException{		
		super(env, r);
		this.env = env;
		viewRule = (ViewRule)r;
		this.userSession = userSession;	
		captionTextSupplier = new SourceSupplier(env, lang);
		this.lang = lang;
		if (viewRule.isOutlineEnable){
			OutlineRule outlineRule = (OutlineRule) env.ruleProvider.getRule(OUTLINE_RULE, viewRule.outlineRuleName);
			outline = new Outline(env, outlineRule, userSession.currentUser);			
		}
	}

	public StringBuffer getContent(HashMap<String, String[]> fields, int page, int pageSize, int docID, int docType, Set<DocID> toExpandResp, Set<String> toExpandCat, DocID toFlash) throws ViewException, DocumentException, DocumentAccessException, RuleException, QueryFormulaParserException, QueryException, LocalizatorException, ComplexObjectException {
		StringBuffer xmlContent = new StringBuffer(5000);

		if (viewRule.isOutlineEnable){
			String oc = userSession.getOutline(outline.toString(), lang);
			if (oc == null){
				String c = outline.getAsXML(lang);
				xmlContent.append(c);	
				userSession.setOutline(outline.toString(), lang, c);
			}else{
				xmlContent.append(oc);
			}			

		}

		xmlContent.append("<columns>");
		for(ViewColumnRule vcr: viewRule.cols.values()){
			if (vcr.hasCaptionValue){
				xmlContent.append("<column" + captionTextSupplier.getValueAsIdAttr(vcr.captionId) + captionTextSupplier.getValueAsCaption(vcr.captionValueSource, vcr.captionValue).toAttrValue() + "></column>");
			}
		}
		xmlContent.append("</columns>");

		FormActions fa = new FormActions(viewRule, captionTextSupplier);
		String actions = fa.getActions(userSession.currentUser, env);
		xmlContent.append(actions);

		String glossarySet = getSpravFieldSet(userSession.currentUser, lang);
		xmlContent.append(glossarySet);

		if (viewRule.isCountEnable){
			xmlContent.append("<counts>");
			for(String ruleName: viewRule.countRuleName){
				QueryRule countQueryRule = (QueryRule) env.ruleProvider.getRule(QUERY_RULE, ruleName);		
				Query query = QueryFactory.getQuery(env, countQueryRule, userSession.currentUser);
				xmlContent.append("<" + ruleName + ">" + query.count(fields) + "</" + ruleName + ">");
			}
			xmlContent.append("</counts>");
		}

		if (viewRule.isQueryEnable){
			QueryRule queryRule = (QueryRule) env.ruleProvider.getRule(QUERY_RULE, viewRule.queryRuleName);
			Query query = QueryFactory.getQuery(env, queryRule, userSession.currentUser);
			query.setQiuckFilter(fields, env);
			int result = query.fetch(page, pageSize, docID, docType, toExpandResp, toExpandCat, toFlash, fields);
			if (result > -1){	
				xmlContent.append(query.toXML());
			}
		}
		xmlContent.append(viewRule.staticXMLContent);
		return xmlContent;

	}
}
