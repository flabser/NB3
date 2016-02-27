package kz.flabs.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kz.flabs.localization.LanguageCode;
import kz.flabs.servlets.Cookies;
import kz.flabs.users.User;
import kz.flabs.workspace.LoggedUser;
import kz.flabs.workspace.WorkSpaceSession;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.scripting._Session;
import kz.lof.webserver.servlet.SessionCooks;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

public class AccessGuard implements Filter {
	private ServletContext context;
	private AppEnv env;

	@Override
	public void init(FilterConfig config) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain) {
		try {
			HttpServletRequest http = (HttpServletRequest) request;
			context = http.getServletContext();
			env = (AppEnv) context.getAttribute(EnvConst.APP_ATTR);

			Cookies appCookies = new Cookies(http);

			String token = appCookies.wAuthHash;
			if (token.length() > 0) {
				LoggedUser logUser = WorkSpaceSession.getLoggeedUser(token);

				if (logUser != null) {
					User user = logUser.getUser();
					String cApp = env.appType.trim();

					if (user.enabledApps.containsKey(cApp) || cApp.equals("Workspace") || cApp.equals("administrator")) {
						chain.doFilter(request, resp);
					} else {
						request.getRequestDispatcher("/Error?type=access_guard_error").forward(request, resp);
						AppEnv.logger.errorLogEntry("For user \"" + user.getUserID() + "\" application '" + cApp + "' access denied");
					}
				} else {
					chain.doFilter(request, resp);
				}
			} else {
				chain.doFilter(request, resp);
			}

		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	private void gettingSession(Request request, Response response, AppEnv env, String appID) {
		HttpSession jses = request.getSession(false);
		if (jses == null) {
			jses = request.getSession(true);
			jses.setAttribute(EnvConst.SESSION_ATTR, getAnonymousSes(request, response, jses, env, appID));
		} else {
			_Session us = (_Session) jses.getAttribute(EnvConst.SESSION_ATTR);
			if (us == null) {
				jses.setAttribute(EnvConst.SESSION_ATTR, getAnonymousSes(request, response, jses, env, appID));
			}
		}

	}

	private _Session getAnonymousSes(Request request, Response response, HttpSession jses, AppEnv env, String appID) {
		SessionCooks cooks = new SessionCooks(request, response);
		_Session ses = new _Session(env, new User());
		ses.setLang(LanguageCode.valueOf(cooks.getCurrentLang()));
		return ses;
	}

	@Override
	public void destroy() {

	}
}
