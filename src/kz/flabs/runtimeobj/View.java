package kz.flabs.runtimeobj;

import java.util.HashMap;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.ViewException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.query.QueryRule;
import kz.flabs.webrule.view.ViewColumnRule;
import kz.flabs.webrule.view.ViewRule;

public class View extends Content implements Const {
	private ViewRule viewRule;
	private UserSession userSession;
	private SourceSupplier captionTextSupplier;
	private AppEnv env;
	private String lang;

	public View(AppEnv env, Rule r, UserSession userSession, String lang) throws RuleException, QueryFormulaParserException {
		super(env, r);
		this.env = env;
		viewRule = (ViewRule) r;
		this.userSession = userSession;
		captionTextSupplier = new SourceSupplier(env, lang);
		this.lang = lang;

	}

	public StringBuffer getContent(HashMap<String, String[]> fields, int page, int pageSize, int docID, int docType, Set<DocID> toExpandResp,
	        Set<String> toExpandCat, DocID toFlash) throws ViewException, DocumentException, DocumentAccessException, RuleException,
	        QueryFormulaParserException, QueryException, LocalizatorException, ComplexObjectException {
		StringBuffer xmlContent = new StringBuffer(5000);

		xmlContent.append("<columns>");
		for (ViewColumnRule vcr : viewRule.cols.values()) {
			if (vcr.hasCaptionValue) {
				xmlContent.append("<column" + captionTextSupplier.getValueAsIdAttr(vcr.captionId)
				        + captionTextSupplier.getValueAsCaption(vcr.captionValueSource, vcr.captionValue).toAttrValue() + "></column>");
			}
		}
		xmlContent.append("</columns>");

		FormActions fa = new FormActions(viewRule, captionTextSupplier);
		String actions = fa.getActions(userSession.currentUser, env);
		xmlContent.append(actions);

		String glossarySet = getSpravFieldSet(userSession.currentUser, lang);
		xmlContent.append(glossarySet);

		if (viewRule.isCountEnable) {
			xmlContent.append("<counts>");
			for (String ruleName : viewRule.countRuleName) {
				QueryRule countQueryRule = (QueryRule) env.ruleProvider.getRule(QUERY_RULE, ruleName);

			}
			xmlContent.append("</counts>");
		}

		if (viewRule.isQueryEnable) {
			QueryRule queryRule = (QueryRule) env.ruleProvider.getRule(QUERY_RULE, viewRule.queryRuleName);

		}
		xmlContent.append(viewRule.staticXMLContent);
		return xmlContent;

	}
}
