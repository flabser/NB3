package kz.nextbase.script.actions;

import java.util.ArrayList;

import kz.flabs.webrule.constants.RunMode;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;

public class _ActionBar implements _IXMLContent {
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
	public String toXML() {
		String a = "";
		for (_Action act : actions) {
			a += act.toXML();
		}
		return "<actionbar mode=\"" + isOn + "\" caption=\"" + caption + "\" hint=\"" + hint + "\">" + a + "</actionbar>";
	}

	@Override
	public String toString() {
		return toXML();
	}

}
