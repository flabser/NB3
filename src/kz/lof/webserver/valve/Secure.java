package kz.lof.webserver.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.flabs.users.User;
import kz.lof.env.EnvConst;
import kz.lof.env.SessionPool;
import kz.lof.webserver.servlet.SessionCooks;
import kz.nextbase.script._Session;
import kz.pchelka.server.Server;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class Secure extends ValveBase {
	RequestURL ru;
	AppEnv env;

	public void invoke(Request request, Response response, RequestURL ru, AppEnv site) throws IOException, ServletException {
		this.ru = ru;
		this.env = site;
		invoke(request, response);
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		HttpServletRequest http = request;
		String appType = ru.getAppType();

		if (!appType.equalsIgnoreCase("")) {
			HttpSession jses = http.getSession(false);
			if (jses != null) {
				_Session ses = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);
				if (ses != null && !ses.getUser().getLogin().equals(User.ANONYMOUS_USER)) {
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
				_Session clonedSes = ses.clone(env);
				jses.setAttribute(EnvConst.SESSION_ATTR, clonedSes);
				clonedSes.setJses(jses);
				Server.logger.verboseLogEntry(ses.getUser().getLogin() + "\" got from session pool " + jses.getServletContext().getContextPath());
				invoke(request, response);
			} else {
				Server.logger.warningLogEntry("there is no associated user session for the token");
				AuthFailedException e = new AuthFailedException(AuthFailedExceptionType.NO_ASSOCIATED_SESSION_FOR_THE_TOKEN, ru.getAppType());
				response.setStatus(e.getCode());
				response.getWriter().println(e.getHTMLMessage());
			}
		} else {
			Server.logger.warningLogEntry("user session was expired");
			AuthFailedException e = new AuthFailedException(AuthFailedExceptionType.NO_USER_SESSION, ru.getAppType());
			response.setStatus(e.getCode());
			response.getWriter().println(e.getHTMLMessage());
		}
	}
}
