package kz.flabs.webrule.query;

import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;

public class Categories implements IRuleValue {
	private String value;
	ValueSourceType sourceType;
	FieldType fieldType;

	Categories(String value, String st, String ft){
		this.value = value;
		sourceType = ValueSourceType.valueOf(st);
		fieldType = FieldType.valueOf(ft);
	}

	@Override
	public ValueSourceType getSourceType() {
		return sourceType;
	}

	@Override
	public Enum getValueType() {		
		return fieldType;
	}

	@Override
	public String getValue() {		
		return value;
	}
	
	public String toString(){
		return "value=" + value + ", source=" + sourceType + ", type=" + fieldType;
	}
}
