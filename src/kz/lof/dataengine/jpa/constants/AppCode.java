package kz.lof.dataengine.jpa.constants;

public enum AppCode {
	UNKNOWN(0), ADMINISTRATOR(899), CUSTOM(900), STAFF(901), REFERENCE(902), WORKSPACE(903);

	private int code;

	AppCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static AppCode getType(int code) {
		for (AppCode type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
