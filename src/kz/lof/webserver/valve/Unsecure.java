package kz.lof.webserver.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import kz.flabs.exception.RuleException;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.exception.ApplicationException;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.AnonymousUser;
import kz.lof.webserver.servlet.SessionCooks;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.http.HttpStatus;

public class Unsecure extends ValveBase {
	private RequestURL ru;

	public void invoke(Request request, Response response, RequestURL ru) throws IOException, ServletException {
		this.ru = ru;
		invoke(request, response);
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		String appType = ru.getAppType();
		if (appType.equals(EnvConst.SHARED_RESOURCES_APP_NAME)) {
			getNext().getNext().invoke(request, response);
		} else {
			AppEnv env = Environment.getAppEnv(appType);
			if (env != null) {

				if (ru.isAuthRequest()) {
					// if (request.getMethod().equalsIgnoreCase("POST")) {
					HttpSession jses = request.getSession(true);
					jses.setAttribute(EnvConst.SESSION_ATTR, new _Session(env, new AnonymousUser()));
					getNext().getNext().invoke(request, response);
					// } else {
					// ((Secure) getNext()).invoke(request, response, ru, env);
					// }
				} else {
					if (ru.isPage()) {
						try {
							if (env.ruleProvider.getRule(ru.getPageID()).isAnonymousAccessAllowed()) {
								gettingSession(request, response, env);
								getNext().getNext().invoke(request, response);
							} else {
								((Secure) getNext()).invoke(request, response, appType);
							}

						} catch (RuleException e) {
							Server.logger.errorLogEntry(e.getMessage());
							ApplicationException ae = new ApplicationException(appType, e.getMessage(),
							        new _Session(env, new AnonymousUser()).getLang());
							response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
							response.getWriter().println(ae.getHTMLMessage());
						}
					} else if (ru.isProtected()) {
						((Secure) getNext()).invoke(request, response, appType);
					} else {
						gettingSession(request, response, env);
						getNext().getNext().invoke(request, response);
					}
				}
			} else {
				String msg = "unknown application type \"" + appType + "\"";
				Server.logger.warningLogEntry(msg);
				ApplicationException ae = new ApplicationException(ru.getAppType(), msg, LanguageCode.valueOf(EnvConst.DEFAULT_LANG));
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println(ae.getHTMLMessage());
			}

		}
	}

	private void gettingSession(Request request, Response response, AppEnv env) {
		HttpSession jses = request.getSession(false);
		if (jses == null) {
			jses = request.getSession(true);
			jses.setAttribute(EnvConst.SESSION_ATTR, getAnonymousSes(request, response, jses, env));
		} else {
			_Session us = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);
			if (us == null) {
				jses.setAttribute(EnvConst.SESSION_ATTR, getAnonymousSes(request, response, jses, env));
			}
		}

	}

	private _Session getAnonymousSes(Request request, Response response, HttpSession jses, AppEnv env) {
		SessionCooks cooks = new SessionCooks(request, response);
		_Session ses = new _Session(env, new AnonymousUser());
		ses.setLang(LanguageCode.valueOf(cooks.getCurrentLang()));
		return ses;
	}

}
