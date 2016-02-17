package kz.nextbase.script;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.runtimeobj.xml.XMLDocument;
import kz.lof.scripting.IPOJOObject;

@Deprecated
public class _XMLDocument implements _IXMLContent, IPOJOObject {
	XMLDocument document;

	public _XMLDocument(_Tag rootTag) {
		document = new XMLDocument(rootTag.getRuntimeTag());
	}

	public XMLDocument getDocument() {
		return document;
	}

	@Override
	public String toXML() {
		return document.toXML();
	}

	@Override
	public String toString() {
		return toXML();
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	public UUID getId() {
		return null;
	}

	@Override
	public String getURL() {
		return null;
	}

	@Override
	public String getFullXMLChunk(LanguageType lang) {
		return document.toXML();
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return document.toXML();
	}

	@Override
	public boolean isEditable() {
		return false;
	}

}
