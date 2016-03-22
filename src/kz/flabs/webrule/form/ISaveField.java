package kz.flabs.webrule.form;

import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;

public interface ISaveField {
	String getName();

	FieldType getType();

	ValueSourceType getSourceType();

	String getValue();

	String getIfErrorValue();
}
