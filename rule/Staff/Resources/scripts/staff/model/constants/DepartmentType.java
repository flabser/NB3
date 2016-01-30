package staff.model.constants;

public enum DepartmentType {
	UNKNOWN(0), DEPARTMENT(1200), SECTOR(1201);

	private int code;

	DepartmentType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static DepartmentType getType(int code) {
		for (DepartmentType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}

}
