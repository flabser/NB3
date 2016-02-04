package kz.nextbase.script;

import java.util.UUID;

import kz.flabs.localization.LanguageType;

public class _POJOObjectWrapper implements _IPOJOObject {
	private _IPOJOObject object;
	private LanguageType lang;

	public _POJOObjectWrapper(_IPOJOObject object, LanguageType lang) {
		this.object = object;
		this.lang = lang;
	}

	@Override
	public UUID getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public _URL getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullXMLChunk(LanguageType lang) {
		return "<document entity=\"" + object.getClass().getSimpleName().toLowerCase() + "\"  docid=\"" + object.getId() + "\" editable=\""
		        + object.isEditable() + "\"><fields>" + object.getFullXMLChunk(lang) + "</fields></document>";
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEditable() {
		return false;
	}
}
