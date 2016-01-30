package reference.model.constants;

public enum LocalityType {
	UNKNOWN(0), CITY(1100), VILLAGE(1101);

	private int code;

	LocalityType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static LocalityType getType(int code) {
		for (LocalityType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}

}
