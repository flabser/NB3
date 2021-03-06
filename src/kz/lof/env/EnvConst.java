package kz.lof.env;

import java.nio.file.Paths;

import kz.lof.localization.LanguageCode;

/**
 *
 *
 * @author Kayra created 23-01-2016
 */

// TODO need to secure this class
public class EnvConst {
	public static final String FRAMEWORK_NAME = "NB3";
	public static final String SUPPOSED_CODE_PAGE = "utf-8";
	public static final String DEFAULT_XML_ENC = "utf-8";
	public static final String APP_ATTR = "app";
	public static final String DEFAULT_PAGE = "index";
	public final static String SESSION_ATTR = "usersession";
	public static final String LANG_COOKIE_NAME = "lang";
	public static final String PAGE_SIZE_COOKIE_NAME = "pagesize";
	public static final String AUTH_COOKIE_NAME = "nb3ses";
	public static final String ADMINISTRATOR_APP_NAME = "Administrator";
	public final static String SHARED_RESOURCES_APP_NAME = "SharedResources";
	public static final String ERROR_XSLT = "error.xsl";
	public static final String FSID_FIELD_NAME = "fsid";
	public static final String TIME_FIELD_NAME = "time";
	public static final String JDBC_DRIVER = "org.postgresql.Driver";
	public static final int DEFAULT_HTTP_PORT = 38700;
	public static final String ADMINISTRATOR_SERVICE_CLASS = "staff.services.UserServices";
	public static final String OFFICEFRAME = "Officeframe";
	public static final String[] OFFICEFRAME_APPS = { "Staff", "Reference", "Workspace" };

	public static String JPA_LOG_LEVEL = "OFF";
	public static String DEFAULT_LANG = LanguageCode.ENG.name();
	public static int DEFAULT_PAGE_SIZE = 20;
	public static String DEFAULT_COUNTRY_OF_NUMBER_FORMAT = "ru";
	public static String DB_USER = "postgres";
	public static String DB_PWD = "smartdoc";
	public static String APP_DB_USER = DB_USER;
	public static String APP_DB_PWD = DB_PWD;
	public static String DATABASE_HOST = "127.0.0.1";
	public static String CONN_PORT = "5432";
	public static String APP_NAME = Paths.get(System.getProperty("user.dir")).getFileName().toString();
	public static String STAFF_APP_NAME = "Staff";
	public static String STAFF_DAO_CLASS = "staff.dao.EmployeeDAO";
	public static String RESOURCES_DIR = "resources";
	public static String NB_JAR_FILE = "nb.jar";
	public static String OLD_STRUCTDB_USER = "postgres";
	public static String OLD_STRUCTDB_PWD = "";
	// public static String STRUCTDB_URL =
	// "jdbc:postgresql://172.16.251.7:5432/CIS";
	public static String OLD_STRUCTDB_URL = "";
}
