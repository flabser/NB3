package kz.flabs.localization;

/**
 * https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes
 *
 */
public enum LanguageType {
	UNKNOWN(0), ENG(45), RUS(570), KAZ(255), BUL(115), POR(545), SPA(230), CHI(315), DEU(316), @Deprecated
	CHN(3150), @Deprecated
	CHO(3151);

	private int code;

	LanguageType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static LanguageType getType(int code) {
		for (LanguageType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
