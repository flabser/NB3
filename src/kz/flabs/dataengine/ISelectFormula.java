package kz.flabs.dataengine;

import java.util.Set;

public interface ISelectFormula {

	enum ReadCondition {
		ONLY_READ(1001), ONLY_UNREAD(1002), ALL(1003);
		private int code;

		ReadCondition(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static ReadCondition getType(int code) {
			for (ReadCondition type : values()) {
				if (type.code == code) {
					return type;
				}
			}
			return ALL;
		}
	}

	@Deprecated
	String getCondition(Set<String> complexUserID, int pageSize, int offset, String[] filters, String[] sorting, boolean checkResponse);

	@Deprecated
	String getCountCondition(Set<String> complexUserID, String[] filters);

}
