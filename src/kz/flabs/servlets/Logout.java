package kz.flabs.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.h2.LoginModeType;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.PortalException;
import kz.flabs.users.UserSession;
import kz.flabs.users.UserSession.HistoryEntry;
import kz.pchelka.env.Environment;

public class Logout extends HttpServlet implements Const {
	private static final long serialVersionUID = 1L;
	private AppEnv env;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		env = (AppEnv) context.getAttribute("portalenv");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		UserSession userSession = null;

		String mode = request.getParameter("mode");
		if (mode == null) {
			mode = "leave_ses";
		}

		try {
			HttpSession jses = request.getSession(false);
			if (jses != null) {
				userSession = (UserSession) jses.getAttribute("usersession");
				// User user = (User)jses.getAttribute("user");
				if (userSession != null) {

				}

				if (env != null && !env.isSystem) {
					String addParameters = "&autologin=0";
					if (mode != null && mode.equalsIgnoreCase("session_lost")) {
						addParameters = "&reason=session_lost&autologin=0";
					} else {
						addParameters = "&reason=user_logout";

						if (env.isWorkspace) {
							Cookie wLoginCook = new Cookie("wauth", "0");
							wLoginCook.setMaxAge(0);
							wLoginCook.setPath("/");
							response.addCookie(wLoginCook);
						} else {
							Cookie loginCook = new Cookie("auth", "0");
							loginCook.setMaxAge(0);
							response.addCookie(loginCook);
						}

						if (userSession != null) {

							UserApplicationProfile userAppProfile = userSession.currentUser.enabledApps.get(env.appType);
							if (userAppProfile != null && userAppProfile.loginMod == LoginModeType.LOGIN_AND_REDIRECT) {

							}

							HistoryEntry entry = userSession.history.getLastEntry();
							Cookie ruCookie = new Cookie("ru", entry.URL);
							ruCookie.setMaxAge(99999);
							response.addCookie(ruCookie);
						}

					}
				}
				jses.invalidate();
				jses = null;
			}
			response.sendRedirect(getRedirect());
		} catch (Exception e) {
			new PortalException(e, env, response, ProviderExceptionType.LOGOUTERROR);
		}

	}

	private String getRedirect() {
		if (env != null) {
			if (env.isSystem) {
				return "";
			} else {
				if (Environment.workspaceAuth) {
					return /* Environment.getFullHostName() + */"/Workspace/Provider?type=page&id=workspace";
				} else {
					return env.globalSetting.entryPoint + "&reason=session_lost&autologin=0";
				}
			}
		} else {
			return "";
		}
	}

}
