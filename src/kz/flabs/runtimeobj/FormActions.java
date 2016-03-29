package kz.flabs.runtimeobj;

import kz.flabs.exception.DocumentException;
import kz.flabs.exception.RuleException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.form.FormActionRule;
import kz.flabs.webrule.form.FormRule;
import kz.lof.appenv.AppEnv;

public class FormActions {
	private Rule rule;
	private SourceSupplier captionTextSupplier;
	private SourceSupplier ss;

	FormActions(FormRule rule, SourceSupplier captionTextSupplier) {
		this.rule = rule;
		this.captionTextSupplier = captionTextSupplier;
	}

	public String getActions(BaseDocument doc, User currentUser) throws DocumentException, RuleException {
		String caption = "";
		StringBuffer xmlText = new StringBuffer(100);
		ss = new SourceSupplier(doc, currentUser, doc.getAppEnv());

		for (FormActionRule entry : rule.showActionsMap.values()) {
			xmlText.append("<action ");

			if (entry.hasCaptionValue) {
				caption = captionTextSupplier.getValueAsCaption(entry.captionValueSource, entry.captionValue).toAttrValue();
			}

			xmlText.append(caption + ">" + entry.type + "</action>");

		}
		return xmlText.toString();
	}

	public String getActions(User currentUser, AppEnv env) throws DocumentException, RuleException {
		String caption = "";
		StringBuffer xmlText = new StringBuffer(100);
		ss = new SourceSupplier(null, currentUser, env);

		for (FormActionRule entry : rule.defaultActionsMap.values()) {
			xmlText.append("<action ");

			if (entry.hasCaptionValue) {
				caption = captionTextSupplier.getValueAsCaption(entry.captionValueSource, entry.captionValue).toAttrValue();
			}

			xmlText.append(caption + ">" + entry.type + "</action>");
		}
		return xmlText.toString();
	}

	private boolean isGranted(FormActionRule entry, String userID) throws DocumentException, RuleException {

		return false;
	}

}
