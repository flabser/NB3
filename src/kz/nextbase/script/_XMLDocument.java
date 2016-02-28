package kz.nextbase.script;

import kz.flabs.runtimeobj.xml.XMLDocument;
import kz.lof.webserver.servlet.IOutcomeObject;

@Deprecated
public class _XMLDocument implements _IXMLContent, IOutcomeObject {
	XMLDocument document;

	@Deprecated
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
	public Object toJSON() {
		return null;
	}

}
