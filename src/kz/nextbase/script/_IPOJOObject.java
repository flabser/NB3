package kz.nextbase.script;

import java.util.UUID;

import kz.flabs.localization.LanguageType;

public interface _IPOJOObject {
	UUID getId();

	_URL getURL();

	String getFullXMLChunk(LanguageType lang);

	String getShortXMLChunk(LanguageType lang);

	public boolean isEditable();

}
