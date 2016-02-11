package kz.lof.dataengine.jpadatabase.ftengine;

import kz.flabs.localization.LanguageType;

public class ToFTIndex {
	private String tableName;
	private String fieldName;
	private LanguageType lang;

	public ToFTIndex(String tableName, String fieldName, LanguageType lang) {
		super();
		this.tableName = tableName;
		this.fieldName = fieldName;
		this.lang = lang;
	}

	public String getTableName() {
		return tableName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getLang() {
		switch (lang) {
		case RUS:
			return "russian";
		default:
			return "english";
		}

	}

}
