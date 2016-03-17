package kz.flabs.dataengine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import kz.flabs.users.User;

public interface Const {
	String[] supervisorGroup = { "[supervisor]" };
	List<String> supervisorGroupAsList = Arrays.asList(supervisorGroup);
	HashSet<String> supervisorGroupAsSet = new HashSet<>(supervisorGroupAsList);
	@Deprecated
	String[] observerGroup = { "[observer]" };
	@Deprecated
	List<String> observerGroupAsList = Arrays.asList(observerGroup);
	@Deprecated
	HashSet<String> sysGroupAsSet = new HashSet<>(observerGroupAsList);
	/**
	 * @deprecated use {@link #kz.lof.user.SuperUser()} instead.
	 */
	@Deprecated
	String sysUser = "supervisor";
	String DEFAULT_SORT_ORDER = "ASC";
	String DEFAULT_SORT_COLUMN = "VIEWDATE";
	User supervisorUser = new User(sysUser);

	int UNKNOWN = 0;
	int TEXT = 1;
	int DATETIMES = 2;
	int NUMBERS = 3;
	int AUTHORS = 4;
	@Deprecated
	int TEXTLIST = 5;
	int READERS = 6;
	int FILES = 7;
	@Deprecated
	int GLOSSARY = 8;
	int DATE = 9;
	int COMPLEX_OBJECT = 10;
	int RICHTEXT = 11;
	int COORDINATION = 12;

	int EDITMODE_NOACCESS = 140;
	int EDITMODE_READONLY = 141;
	int EDITMODE_EDIT = 142;

	int CATEGORY = 887;
	int DOCTYPE_DEPARTMENT = 888;
	int DOCTYPE_EMPLOYER = 889;
	int DOCTYPE_UNKNOWN = 890;
	int DOCTYPE_ORGANIZATION = 891;
	int DOCTYPE_PERSON = 893;
	int DOCTYPE_GLOSSARY = 894;
	int DOCTYPE_USERPROFILE = 895;
	int DOCTYPE_MAIN = 896;
	int DOCTYPE_TASK = 897;
	int DOCTYPE_EXECUTION = 898;
	int DOCTYPE_PROJECT = 899;
	int DOCTYPE_GROUP = 900;
	int DOCTYPE_REPORT = 901;
	int DOCTYPE_ACTIVITY_ENTRY = 902;
	int DOCTYPE_RECYCLE_BIN_ENTRY = 903;
	int DOCTYPE_TOPIC = 904;
	int DOCTYPE_POST = 905;
	int DOCTYPE_COORD_COMMENT = 906;

	int DATABASE_EMBEDED = 1300;

	int SCHEDULER_OFFLINE = 3000;
	int SCHEDULER_PERIODICAL = 3001;
	int SCHEDULER_INTIME = 3002;

	int DOCTYPE_ACCOUNT = 906;
}
