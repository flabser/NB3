package kz.nextbase.script.actions;

import kz.flabs.localization.Vocabulary;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.scripting._Session;

public class _Action {
	public RunMode isOn = RunMode.ON;
	public String caption;
	public String hint;
	public _ActionType type;
	public String customID;

	private String url = "";

	public _Action(_ActionType type) {
		this.type = type;
		switch (type) {
		case CLOSE:
			caption = "close";
			hint = "just_close";
			break;
		case BACK:
			caption = "back";
			hint = "just_back";
			break;
		case GET_DOCUMENT_ACCESSLIST:
			caption = "get_document_acl";
			hint = "who_can_read_and_edit_the_document";
			break;
		default:
			caption = "";
			hint = "";
		}
		customID = type.toString().toLowerCase();
	}

	public _Action(String caption, String hint, _ActionType type) {
		this.caption = caption;
		this.hint = hint;
		this.type = type;
		customID = type.toString().toLowerCase();
	}

	public _Action(String caption, String hint, String customID) {
		this.caption = caption;
		this.hint = hint;
		this.type = _ActionType.CUSTOM_ACTION;
		this.customID = customID;
	}

	public void setURL(String u) {
		url = u;
	}

	public String toXML() {
		return "<action  mode=\"" + isOn + "\"" + XMLUtil.getAsAttribute("url", url) + " id=\"" + customID + "\" caption=\"" + caption + "\" hint=\""
		        + hint + "\">" + type + getJson(type) + "</action>";
	}

	void setSession(_Session session) {
		Vocabulary v = session.getAppEnv().vocabulary;
		caption = v.getWord(caption, session.getLang());
		hint = v.getWord(hint, session.getLang());
	}

	private String getJson(_ActionType type) {
		switch (type) {
		case CLOSE:
			return "<js><![CDATA[window.history.back()]]></js>";
		case BACK:
			return "<js><![CDATA[window.history.back()]]></js>";
		case GET_DOCUMENT_ACCESSLIST:

		default:
			return "";
		}
	}
	// Provider?type=page&id=accesslist&docid=3W93088orWwWo63r
}
