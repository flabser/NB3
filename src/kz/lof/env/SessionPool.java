package kz.lof.env;

import java.nio.charset.Charset;
import java.util.HashMap;

import kz.flabs.util.Util;
import kz.lof.scripting._Session;

import org.apache.commons.codec.binary.Base64;

public class SessionPool {
	private static HashMap<Integer, _Session> userSessions = new HashMap<Integer, _Session>();

	public static String put(_Session us) {
		String sesID = Util.generateRandomAsText();
		int key = Base64.encodeBase64String(us.getUser().getLogin().getBytes(Charset.forName("UTF-8"))).hashCode();
		userSessions.put(key, us);
		String token = sesID + "#" + key;
		return token;
	}

	public static _Session getLoggeedUser(String token) {
		int key = 0;
		try {
			key = Integer.parseInt(token.substring(token.indexOf("#") + 1, token.length()));
		} catch (NumberFormatException e) {

		}
		_Session us = userSessions.get(key);
		if (us != null) {
			return us;
		} else {
			return null;
		}
	}

	public static void remove(_Session us) {
		userSessions.remove(us.getUser().getLogin());
	}

	public static HashMap<Integer, _Session> getUserSessions() {
		return userSessions;
	}

}
