package kz.nextbase.script.actions;

import java.util.ArrayList;
import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.webrule.constants.RunMode;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._IPOJOObject;
import kz.nextbase.script._Session;
import kz.nextbase.script._URL;

public class _ActionBar implements _IPOJOObject {
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

	@Override
	public UUID getId() {
		return null;
	}

	@Override
	public _URL getURL() {
		return null;
	}

	@Override
	public String getFullXMLChunk(LanguageType lang) {
		String a = "";
		for (_Action act : actions) {
			a += act.toXML();
		}
		return "<actionbar mode=\"" + isOn + "\" caption=\"" + caption + "\" hint=\"" + hint + "\">" + a + "</actionbar>";
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return getFullXMLChunk(lang);
	}

	@Override
	public boolean isEditable() {
		return false;
	}

}
