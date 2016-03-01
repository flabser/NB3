package kz.lof.webserver.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kz.lof.env.EnvConst;
import kz.lof.localization.LanguageCode;

public class SessionCooks {

	public String auth;
	private String currentLang;
	private HttpServletResponse response;
	private static final int MONTH_TIME = 60 * 60 * 24 * 365;

	public SessionCooks(HttpServletRequest request, HttpServletResponse response) {
		this.response = response;
		try {
			Cookie[] cooks = request.getCookies();
			if (cooks != null) {
				for (int i = 0; i < cooks.length; i++) {
					if (cooks[i].getName().equals(EnvConst.LANG_COOKIE_NAME)) {
						currentLang = cooks[i].getValue();
					} else if (cooks[i].getName().equals(EnvConst.AUTH_COOKIE_NAME)) {
						auth = cooks[i].getValue();
					}
				}
			}
		} catch (Exception e) {

		}
	}

	public void saveLang(LanguageCode lang) {
		Cookie c = new Cookie(EnvConst.LANG_COOKIE_NAME, lang.name());
		c.setMaxAge(MONTH_TIME);
		// c.setDomain("/");
		response.addCookie(c);
	}

	public String getCurrentLang() {
		if (currentLang == null) {
			return LanguageCode.ENG.name();
		} else {
			return currentLang;
		}
	}

	@Override
	public String toString() {
		return "lang=" + currentLang;
	}

}
