package kz.lof.scripting;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script._URL;

public interface IPOJOObject {
	UUID getId();

	_URL getURL();

	String getFullXMLChunk(LanguageType lang);

	String getShortXMLChunk(LanguageType lang);

	public boolean isEditable();

}
