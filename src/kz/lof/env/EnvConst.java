package kz.lof.env;

import java.io.File;

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
	public static final String AUTH_COOKIE_NAME = "nb3ses";
	public static final String ADMINISTRATOR_APP_NAME = "Administrator";
	public final static String SHARED_RESOURCES_APP_NAME = "SharedResources";
	public static final String ERROR_XSLT = "xsl" + File.separator + "errors" + File.separator + "error.xsl";
	public static final String FSID_FIELD_NAME = "fsid";
	public static final String TIME_FIELD_NAME = "time";

	public static String STAFF_APP_NAME = "Staff";
	public static String STAFF_DAO_CLASS = "staff.dao.EmployeeDAO";
	public static String RESOURCES_DIR = "resources";
	public static String NB_JAR_FILE = "nb.jar";
}
