package kz.flabs.localization;

/**
 * https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes
 *
 */
public enum LanguageCode {
	UNKNOWN(0), ENG(45), RUS(570), KAZ(255), BUL(115), POR(545), SPA(230), CHI(315), DEU(316), @Deprecated
	CHN(3150), @Deprecated
	CHO(3151);

	private int code;

	LanguageCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static LanguageCode getType(int code) {
		for (LanguageCode type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
