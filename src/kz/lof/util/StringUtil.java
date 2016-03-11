package kz.lof.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	public static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";

	public static boolean checkByPattren(String value, String p) {
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}
}
