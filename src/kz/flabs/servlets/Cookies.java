package kz.flabs.servlets;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import kz.lof.env.EnvConst;

@Deprecated
public class Cookies {
	public boolean wAuthCookiesIsValid;
	public boolean authCookiesIsValid;
	public String currentLang = EnvConst.DEFAULT_LANG.name();
	public String currentSkin;
	public String wAuthHash = "0";
	public String authHash = "0";
	public int pageSize = 20;
	public String redirectURL;
	public boolean redirectURLIsValid;

	@Deprecated
	public Cookies(HttpServletRequest request) {
		Cookie[] cooks = request.getCookies();
		if (cooks != null) {
			for (int i = 0; i < cooks.length; i++) {
				if (cooks[i].getName().equals(EnvConst.LANG_COOKIE_NAME)) {
					currentLang = cooks[i].getValue();
				} else if (cooks[i].getName().equals("wauth")) {
					wAuthHash = cooks[i].getValue();
					wAuthCookiesIsValid = true;
				} else if (cooks[i].getName().equals("auth")) {
					authHash = cooks[i].getValue();
					authCookiesIsValid = true;
				} else if (cooks[i].getName().equals("ru")) {
					redirectURL = cooks[i].getValue();
					redirectURLIsValid = true;
				} else if (cooks[i].getName().equals("pagesize")) {
					try {
						pageSize = Integer.parseInt(cooks[i].getValue());
					} catch (NumberFormatException nfe) {
						pageSize = 20;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "auth=" + authHash + ", wauth=" + wAuthHash + ", " + currentLang + "," + currentSkin + ", " + pageSize;
	}

}
