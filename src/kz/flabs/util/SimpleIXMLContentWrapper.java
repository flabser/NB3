package kz.flabs.util;

import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;

public class SimpleIXMLContentWrapper implements _IXMLContent {
	private String xmlPiece;

	public SimpleIXMLContentWrapper(String xmlPiece) {
		this.xmlPiece = xmlPiece;
	}

	@Override
	public String toXML() throws _Exception {
		return xmlPiece;
	}

	@Override
	public Object toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}