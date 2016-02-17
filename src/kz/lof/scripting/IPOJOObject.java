package kz.lof.scripting;

import java.util.UUID;

import kz.flabs.localization.LanguageType;

public interface IPOJOObject {
	UUID getId();

	String getURL();

	String getFullXMLChunk(LanguageType lang);

	String getShortXMLChunk(LanguageType lang);

	public boolean isEditable();

}
