package kz.flabs.dataengine;

import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.RunTimeParameters.Filter;
import kz.flabs.users.RunTimeParameters.Sorting;
import kz.flabs.users.User;

import java.util.HashSet;
import java.util.Set;

public interface ISelectFormula {

    String getCountForPaging(Set<String> users, Set<Filter> filters);


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
	String getCountCondition(Set<String> complexUserID,String[] filters);

    String getCondition(Set<String> complexUserID, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse);

    String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, boolean checkRead);

    String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, ISelectFormula.ReadCondition condition);
    String getCondition(User user, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, ISelectFormula.ReadCondition condition, String customFieldName);

    String getCountCondition(Set<String> complexUserID, Set<Filter> filters);

    String getCondition(Set<String> users, int pageSize, int offset, Set<Filter> filters, Set<Sorting> sorting, boolean checkResponse, String responseQueryCondition);

    String getCountCondition(User user, Set<Filter> filters, ISelectFormula.ReadCondition readCondition);

    String getCountCondition(User user, Set<Filter> filters, ISelectFormula.ReadCondition readCondition, String customFieldName);
}
