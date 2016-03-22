package kz.flabs.webrule.form;

import groovy.lang.GroovyObject;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;

public interface IShowField {
	ValueSourceType getSourceType();

	String getValue();

	Class<GroovyObject> getCompiledClass();

	String getName();

	TagPublicationFormatType getPublishAs();

	boolean hasAttrValue();

	ValueSourceType getAttrSourceType();

	String getAttrValue();

	TagPublicationFormatType getAttrValueType();

	String getCaptionValue();

	ValueSourceType getCaptionValueSource();

}
