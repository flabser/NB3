package kz.flabs.users;

import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.ISaveField;

public class SaveField implements ISaveField {
	private String name;
	private String value;

	public SaveField(String value) {
		this.name = value;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public FieldType getType() {
		return FieldType.TEXT;
	}

	@Override
	public ValueSourceType getSourceType() {
		return ValueSourceType.WEBFORMFIELD;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getIfErrorValue() {
		return "";
	}

}
