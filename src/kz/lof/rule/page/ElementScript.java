package kz.lof.rule.page;

import kz.flabs.webrule.constants.ValueSourceType;

public class ElementScript {
	private String className;
	private ValueSourceType type;

	public ElementScript(ValueSourceType type, String className) {
		this.type = type;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public ValueSourceType getType() {
		return type;
	}

}
