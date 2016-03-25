package kz.lof.localization;

/**
 * https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes
 *
 */
public enum LanguageCode {
	UNKNOWN(0, "english"), ENG(45, "english"), RUS(570, "russian"), KAZ(255, "english"), BUL(115, "english"),
	POR(545, "portuguese"), SPA(230, "spanish"), CHI(315, "english"), DEU(316, "german"), @Deprecated
	CHN(3150, "english"), @Deprecated
	CHO(3151, "english");

	private int code;
	private String lang;

	LanguageCode(int code, String lang) {
		this.code = code;
		this.lang = lang;
	}

	public int getCode() {
		return code;
	}
	public String getLang(){
		return lang;
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
