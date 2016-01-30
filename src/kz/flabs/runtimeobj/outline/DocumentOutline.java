package kz.flabs.runtimeobj.outline;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentException;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.outline.OutlineRule;

public class DocumentOutline extends Outline implements IOutline {

	private String XMLText = "";
	private String key;

	public DocumentOutline(AppEnv env, OutlineRule outlineRule, String id, String key, int page, UserSession userSession, String element) {
		super(env, outlineRule, "edit", id, page, userSession, element);
		this.key = key;
	}

	public String getOutlineAsXML(String lang) throws DocumentException {
		XMLText += "<current type=\"" + type + "\" id=\"" + id + "\" key=\""
				+ key + "\" page=\"" + page + "\">" + getTitle(type)
				+ "</current>";
		XMLText += "<outline>";
		XMLText += getNavigationPanel(outlineRule.getOutlineRootEntry(), lang,
				env.vocabulary, "Provider?type=outline&subtype=" + type
						+ "&id=" + id);
		XMLText += getSetOfFieldsAsXML(lang);
		XMLText += "</outline>";
		XMLText += getCurrentUserProperty(lang);

		return XMLText;
	}

	protected String getTitle(String type) {
		return prefix + "**";
	}
}
