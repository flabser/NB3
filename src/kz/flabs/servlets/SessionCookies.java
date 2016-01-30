package kz.flabs.servlets;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class SessionCookies {
	public String lang = "UNKNOWN", skin = "classic";
	

	SessionCookies(HttpServletRequest request){		
		Cookie[] cooks = request.getCookies();
		if (cooks != null){
			for (int i = 0; i < cooks.length; i++){
				Cookie cookie = cooks[i];
				if (cookie.getName().equalsIgnoreCase("lang")){
					lang = cookie.getValue().toUpperCase();
				}else if (cookie.getName().equalsIgnoreCase("skin")){
					skin = cookie.getValue();
				}
			}
		}
		if (lang == "UNKNOWN" || lang.length() == 0) lang = "RUS";
	}
}
