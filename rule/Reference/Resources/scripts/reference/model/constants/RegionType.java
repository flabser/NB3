package reference.model.constants;

/**
 * 
 * 
 * @author Kayra created 28-12-2015
 */

public enum RegionType {
	UNKNOWN(0), URBAN_AGGLOMERATION(601), REGION(602), FEDERATION(603);

	private int code;

	RegionType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static RegionType getType(int code) {
		for (RegionType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
