package kz.lof.env;

import java.io.File;

import kz.lof.localization.LanguageCode;

/**
 *
 *
 * @author Kayra created 23-01-2016
 */

// TODO need to secure this class
public class EnvConst {
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
	public static final String ERROR_XSLT = "xsl" + File.separator + "errors" + File.separator + "error.xsl";
	public static final String FSID_FIELD_NAME = "fsid";
	public static final String TIME_FIELD_NAME = "time";
	public static final String JDBC_DRIVER = "org.postgresql.Driver";
	public static final int DEFAULT_HTTP_PORT = 38700;
	public static final String ADMINISTRATOR_SERVICE_CLASS = "administrator.services.UserServices";

	public static String DEFAULT_LANG = LanguageCode.ENG.name();
	public static String DEFAULT_PAGE_SIZE = "20";
	public static String DB_USER = "postgres";
	public static String DB_PWD = "smartdoc";
	public static String DATABASE_HOST = "localhost";
	public static String CONN_PORT = "5432";
	public static String STAFF_APP_NAME = "Staff";
	public static String STAFF_DAO_CLASS = "staff.dao.EmployeeDAO";
	public static String RESOURCES_DIR = "resources";
	public static String NB_JAR_FILE = "nb.jar";
}
