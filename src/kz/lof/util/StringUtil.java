package kz.lof.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.realm.RealmBase;

public class StringUtil {
	public static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";

	public static boolean checkByPattren(String value, String p) {
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}

	public static String encode(String val) {
		return RealmBase.Digest(val, "MD5", "UTF-8");
	}
}
