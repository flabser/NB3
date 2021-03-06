package kz.lof.rule.page;

public enum ElementType {
	UNKNOWN(1000), SCRIPT(1002), INCLUDED_PAGE(1003);

	private int code;

	ElementType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ElementType getType(int code) {
		for (ElementType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
