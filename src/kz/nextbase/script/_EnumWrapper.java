package kz.nextbase.script;

import java.util.UUID;

import kz.lof.scripting.IPOJOObject;
import kz.lof.scripting._Session;

/**
 * wrapp a Enum to publish as XML
 *
 * @author Kayra created 03-01-2016
 */

public class _EnumWrapper<T extends Enum<?>> implements IPOJOObject {
	private T[] enumObj;
	private String[] translatedWords;

	public _EnumWrapper(T[] enumObj) {
		this.enumObj = enumObj;
		translatedWords = new String[enumObj.length];

		for (int i = 0; i < enumObj.length; i++) {
			translatedWords[i] = enumObj[i].name();
		}
	}

	public _EnumWrapper(T[] enumObj, String[] translatedWords) {
		this.enumObj = enumObj;
		this.translatedWords = translatedWords;
	}

	@Override
	public UUID getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullXMLChunk(_Session ses) {
		final Class<T> enumClass = (Class<T>) enumObj[0].getClass();
		String entityType = enumClass.getSimpleName().toLowerCase();
		StringBuffer res = new StringBuffer(1000).append("<constants entity=\"" + entityType + "\">");
		for (int i = 0; i < enumObj.length; i++) {
			res.append("<entry attrval=\"" + enumObj[i] + "\">" + translatedWords[i] + "</entry>");
		}

		return res.append("</constants>").toString();
	}

	@Override
	public String getShortXMLChunk(_Session ses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}
}
