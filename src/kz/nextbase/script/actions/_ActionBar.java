package kz.nextbase.script.actions;

import java.util.ArrayList;
import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.scripting._Session;
import kz.lof.webserver.servlet.IOutcomeObject;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;

public class _ActionBar implements IOutcomeObject {
	public RunMode isOn = RunMode.ON;
	public String caption = "";
	public String hint = "";

	private ArrayList<_Action> actions = new ArrayList<_Action>();
	private _Session session;

	public _ActionBar() throws _Exception {
		throw new _Exception(_ExceptionType.CONSTRUCTOR_UNDEFINED, "Default constructor undefined, you should use  \"new _ActionBar(_Session)\"");
	}

	public _ActionBar(_Session ses) {
		session = ses;
	}

	public _ActionBar addAction(_Action action) {
		action.setSession(session);
		actions.add(action);
		return this;
	}

	public UUID getId() {
		return null;
	}

	public String getURL() {
		return null;
	}

	public String getFullXMLChunk(LanguageType lang) {
		String a = "";
		for (_Action act : actions) {
			a += act.toXML();
		}
		return "<actionbar mode=\"" + isOn + "\" caption=\"" + caption + "\" hint=\"" + hint + "\">" + a + "</actionbar>";
	}

	public String getShortXMLChunk(LanguageType lang) {
		return getFullXMLChunk(lang);
	}

	public boolean isEditable() {
		return false;
	}

	@Override
	public String toXML() {
		String a = "";
		for (_Action act : actions) {
			a += act.toXML();
		}
		return "<actionbar mode=\"" + isOn + "\" caption=\"" + caption + "\" hint=\"" + hint + "\">" + a + "</actionbar>";
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
