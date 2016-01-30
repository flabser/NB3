package reference.model.constants;

/**
 * https://en.wikipedia.org/wiki/ISO_3166-2
 * 
 * @author Kayra created 27-12-2015
 */
public enum CountryCode {
	UNKNOWN(0), KZ(777), RU(778), BY(779), UA(780), DE(781), FR(782), TR(783), US(784), CN(785), BG(786), GB(787), JP(788), ES(789), PT(790);

	private int code;

	CountryCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static CountryCode getType(int code) {
		for (CountryCode type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}

}
