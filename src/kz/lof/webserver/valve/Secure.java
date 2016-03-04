package kz.lof.webserver.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.env.SessionPool;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.AnonymousUser;
import kz.lof.webserver.servlet.SessionCooks;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class Secure extends ValveBase {
	String appType;

	public void invoke(Request request, Response response, String appType) throws IOException, ServletException {
		this.appType = appType;
		invoke(request, response);
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		HttpServletRequest http = request;

		if (!appType.equalsIgnoreCase("")) {
			HttpSession jses = http.getSession(false);
			if (jses != null) {
				_Session ses = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);
				if (ses != null && !ses.getUser().getUserID().equals(AnonymousUser.USER_NAME)) {
					getNext().invoke(request, response);
				} else {
					gettingSession(request, response);
				}
			} else {
				gettingSession(request, response);
			}
		} else {
			getNext().invoke(request, response);
		}

	}

	private void gettingSession(Request request, Response response) throws IOException, ServletException {
		HttpServletRequest http = request;
		SessionCooks appCookies = new SessionCooks(http, response);
		String token = appCookies.auth;
		if (token != null) {
			_Session ses = SessionPool.getLoggeedUser(token);
			if (ses != null) {
				HttpSession jses = http.getSession(true);
				RequestURL ru = new RequestURL(http.getRequestURI());
				AppEnv env = Environment.getAppEnv(ru.getAppType());
				_Session clonedSes = ses.clone(env);
				jses.setAttribute(EnvConst.SESSION_ATTR, clonedSes);
				clonedSes.setJses(jses);
				Server.logger.debugLogEntry(ses.getUser().getUserID() + "\" got from session pool " + jses.getServletContext().getContextPath());
				invoke(request, response);
			} else {
				Server.logger.warningLogEntry("there is no associated user session for the token");
				AuthFailedException e = new AuthFailedException(AuthFailedExceptionType.NO_ASSOCIATED_SESSION_FOR_THE_TOKEN, appType);
				response.sendRedirect("Logout");
			}
		} else {
			Server.logger.warningLogEntry("user session was expired");
			AuthFailedException e = new AuthFailedException(AuthFailedExceptionType.NO_USER_SESSION, appType);
			response.setStatus(e.getCode());
			response.getWriter().println(e.getHTMLMessage());
		}
	}
}
