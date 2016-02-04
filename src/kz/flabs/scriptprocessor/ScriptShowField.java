package kz.flabs.scriptprocessor;

import groovy.lang.GroovyObject;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.IShowField;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IPOJOObject;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._URL;

public class ScriptShowField implements IShowField, _IPOJOObject {
	private String name;
	private String value = "";
	private String idValue;
	private boolean hasAttr;
	private String XMLPiece;

	public ScriptShowField(String name, String value, boolean noConvert) {
		this.name = name;
		this.value = value;
		XMLPiece = "<" + name + ">" + (noConvert ? value : XMLUtil.getAsTagValue(value)) + "</" + name + ">";
	}

	public ScriptShowField(String name, _IXMLContent value) throws _Exception {
		this.name = name;
		this.value = value.toString();
		XMLPiece = "<" + name + ">" + value.toXML() + "</" + name + ">";
	}

	public ScriptShowField(String name, String value) {
		this.name = name;
		this.value = value;
		XMLPiece = "<" + name + ">" + XMLUtil.getAsTagValue(value) + "</" + name + ">";
	}

	public ScriptShowField(String name, int idv, String value) {
		this.name = name;
		this.value = value;
		this.idValue = Integer.toString(idv);
		hasAttr = true;
		XMLPiece = "<" + name + " attrval=\"" + idValue + "\">" + XMLUtil.getAsTagValue(value) + "</" + name + ">";
	}

	public ScriptShowField(String name, String idValue, String value) {
		this.name = name;
		this.value = value;
		this.idValue = idValue;
		hasAttr = true;
		XMLPiece = "<" + name + " attrval=\"" + idValue + "\">" + XMLUtil.getAsTagValue(value) + "</" + name + ">";
	}

	@Override
	public ValueSourceType getSourceType() {
		return ValueSourceType.DOCFIELD;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Class<GroovyObject> getCompiledClass() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TagPublicationFormatType getPublishAs() {
		return TagPublicationFormatType.AS_IS;
	}

	@Override
	public Macro getMacro() {
		return Macro.UNKNOWN_MACRO;
	}

	@Override
	public boolean hasAttrValue() {
		return hasAttr;
	}

	@Override
	public ValueSourceType getAttrSourceType() {
		return ValueSourceType.UNKNOWN;
	}

	@Override
	public String getAttrValue() {
		return idValue;
	}

	@Override
	public TagPublicationFormatType getAttrValueType() {
		return TagPublicationFormatType.AS_IS;
	}

	@Override
	public Macro getAttrMacro() {
		return Macro.UNKNOWN_MACRO;
	}

	@Override
	public String getCaptionValue() {
		return "";
	}

	@Override
	public ValueSourceType getCaptionValueSource() {
		return ValueSourceType.UNKNOWN;
	}

	@Override
	public String toString() {
		return XMLPiece;
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
		return XMLPiece;
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return XMLPiece;
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

}
