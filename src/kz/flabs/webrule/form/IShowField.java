package kz.flabs.webrule.form;

import groovy.lang.GroovyObject;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;

public interface IShowField {
	ValueSourceType getSourceType();
	String getValue();	
	Class<GroovyObject> getCompiledClass();
	String getName();
	TagPublicationFormatType getPublishAs();
	Macro getMacro();
	
	boolean hasAttrValue();
	ValueSourceType getAttrSourceType();
	String getAttrValue();
	TagPublicationFormatType getAttrValueType();
	Macro getAttrMacro();
	
	String getCaptionValue();
	ValueSourceType getCaptionValueSource();
	
}
