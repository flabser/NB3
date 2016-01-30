package kz.flabs.webrule;

import kz.flabs.sourcesupplier.Macro;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;

public class ValueAdapter implements IRuleValue {
	public String value = "";
	public ValueSourceType sourceType = ValueSourceType.UNKNOWN;
	public FieldType fieldType = FieldType.UNKNOWN;
	public Macro macro = Macro.UNKNOWN_MACRO;
	
	public ValueSourceType getSourceType() {
		return sourceType;
	}

	
	public String getValue() {
		return value;
	}

	
	public Enum getValueType() {
		return fieldType;
	}
	
	public Macro getMacro() {
		return macro;
	}

	public String toString(){
		return "value=" + value + ", source=" + sourceType + ", type=" + fieldType;
	}
	
}
