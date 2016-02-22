package kz.lof.scripting;

import java.util.UUID;

import kz.flabs.localization.LanguageType;

public class POJOObjectAdapter implements IPOJOObject {

	@Override
	public UUID getId() {
		return null;
	}

	@Override
	public String getURL() {
		return "Provider";
	}

	@Override
	public String getFullXMLChunk(LanguageType lang) {
		return "<object>null</object>";
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
