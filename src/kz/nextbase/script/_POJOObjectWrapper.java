package kz.nextbase.script;

import kz.flabs.localization.LanguageType;

public class _POJOObjectWrapper implements _IXMLContent {
	private _IPOJOObject object;
	private LanguageType lang;

	public _POJOObjectWrapper(_IPOJOObject object, LanguageType lang) {
		this.object = object;
		this.lang = lang;
	}

	@Override
	public String toXML() throws _Exception {
		return "<document entity=\"" + object.getClass().getSimpleName().toLowerCase() + "\"  docid=\"" + object.getId() + "\" editable=\""
		        + object.isEditable() + "\"><fields>" + object.getFullXMLChunk(lang) + "</fields></document>";
	}
}
