package kz.lof.scripting;

import kz.flabs.localization.LanguageType;
import kz.lof.webserver.servlet.IOutcomeObject;

public class _POJOObjectWrapper implements IOutcomeObject {
	private IPOJOObject object;
	private LanguageType lang;

	public _POJOObjectWrapper(IPOJOObject object, LanguageType lang) {
		this.object = object;
		this.lang = lang;
	}

	@Override
	public String toXML() {
		return "<document entity=\"" + object.getClass().getSimpleName().toLowerCase() + "\"  docid=\"" + object.getId() + "\" editable=\""
		        + object.isEditable() + "\"><fields>" + object.getFullXMLChunk(lang) + "</fields></document>";
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}
