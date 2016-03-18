package kz.lof.webserver.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.exception.PortalException;
import kz.flabs.servlets.ProviderExceptionType;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.SessionPool;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.IUser;

public class Login extends HttpServlet implements Const {
	private static final long serialVersionUID = 1L;
	private AppEnv env;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		env = (AppEnv) context.getAttribute(EnvConst.APP_ATTR);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		_Session ses = null;
		try {
			String login = request.getParameter("login");
			String pwd = request.getParameter("pwd");
			HttpSession jses;
			ISystemDatabase systemDatabase = DatabaseFactory.getSysDatabase();

			Cookies appCookies = new Cookies(request);

			IUser<Long> user = systemDatabase.getUser(login, pwd);

			if (user != null && user.isAuthorized()) {
				jses = request.getSession(true);
				ses = new _Session(env, user);
				String token = SessionPool.put(ses);
				ses.setLang(LanguageCode.valueOf(appCookies.currentLang));

				AppEnv.logger.infoLogEntry(user.getUserID() + " has connected");

				String redirect = "";

				jses.setAttribute(EnvConst.SESSION_ATTR, ses);
				Cookie authCookie = new Cookie(EnvConst.AUTH_COOKIE_NAME, token);
				authCookie.setMaxAge(-1);
				authCookie.setPath("/");
				response.addCookie(authCookie);

				CallingPageCookie cpc = new CallingPageCookie(request);
				redirect = cpc.page;
				if (redirect.equals("")) {
					redirect = getRedirect(jses, appCookies);
				} else {
					Cookie cpCookie = new Cookie("calling_page", "0");
					cpCookie.setMaxAge(0);
					cpCookie.setPath("/");
					response.addCookie(cpCookie);
				}
				response.sendRedirect(redirect);

			} else {
				AppEnv.logger.infoLogEntry("Authorization failed, login or password is incorrect -");
				throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
			}
		} catch (AuthFailedException e) {
			// e.printStackTrace();
			try {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				request.getRequestDispatcher("/Error?type=ws_auth_error").forward(request, response);
			} catch (IOException e1) {
				Server.logger.errorLogEntry(e1);
			} catch (ServletException e2) {
				Server.logger.errorLogEntry(e2);
			}
		} catch (IOException ioe) {
			Server.logger.errorLogEntry(ioe);
		} catch (IllegalStateException ise) {
			Server.logger.errorLogEntry(ise);
		} catch (Exception e) {
			new PortalException(e, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML);
		}
	}

	private String getRedirect(HttpSession jses, Cookies appCookies) {
		return "Provider?id=workspace";

	}

	public class CallingPageCookie {
		public String page = "";

		public CallingPageCookie(HttpServletRequest request) {
			Cookie[] cooks = request.getCookies();
			if (cooks != null) {
				for (int i = 0; i < cooks.length; i++) {
					if (cooks[i].getName().equals("calling_page")) {
						page = cooks[i].getValue();
					}
				}
			}
		}
	}

}
