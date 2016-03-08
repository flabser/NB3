package kz.lof.webserver.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import kz.lof.env.EnvConst;

public class Cookies {
	public String currentLang = EnvConst.DEFAULT_LANG;
	public int pageSize;

	public Cookies(HttpServletRequest request) {
		Cookie[] cooks = request.getCookies();
		if (cooks != null) {
			for (int i = 0; i < cooks.length; i++) {
				if (cooks[i].getName().equals(EnvConst.LANG_COOKIE_NAME)) {
					currentLang = cooks[i].getValue();
				} else if (cooks[i].getName().equals(EnvConst.PAGE_SIZE_COOKIE_NAME)) {
					try {
						pageSize = Integer.parseInt(cooks[i].getValue());
					} catch (NumberFormatException nfe) {
						pageSize = Integer.parseInt(EnvConst.DEFAULT_PAGE_SIZE);
					}
				}
			}
		}
	}

}
