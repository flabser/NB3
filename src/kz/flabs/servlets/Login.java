package kz.flabs.servlets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

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
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.PortalException;
import kz.flabs.localization.LanguageType;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.flabs.users.User;
import kz.flabs.workspace.WorkSpaceSession;
import kz.lof.env.EnvConst;
import kz.lof.env.SessionPool;
import kz.nextbase.script._Session;
import kz.pchelka.env.Environment;

import org.apache.catalina.realm.RealmBase;

public class Login extends HttpServlet implements Const {
	private static final long serialVersionUID = 1L;
	private AppEnv env;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		env = (AppEnv) context.getAttribute(EnvConst.APP_ATTR);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		// UserSession userSession = null;
		_Session ses = null;
		try {
			String login = request.getParameter("login");
			String pwd = request.getParameter("pwd");
			String noAuth = request.getParameter("noauth");
			String noHash = request.getParameter("nohash");
			HttpSession jses;
			User user = null;
			ISystemDatabase systemDatabase = DatabaseFactory.getSysDatabase();

			if (env == null || env.isSystem) {
				HashMap<String, User> admins = systemDatabase.getAllAdministrators();
				if (admins.size() > 0) {
					User admin = admins.get(login);
					if (admin != null) {
						if (admin.getPasswordHash() != null) {
							if (!admin.getPasswordHash().equals("")) {
								RealmBase rb = null;// !!!

								if (admin != null
								// &&
								// admin.getPasswordHash().equals(pwd.hashCode()
								// + "")) {
								// &&
								// admin.getPasswordHash().equals(getMD5Hash(pwd)))
								// {
								        && admin.getPasswordHash().equals(rb.Digest(pwd, "MD5", "UTF-8"))) {
									jses = request.getSession(true);
									jses.setAttribute("adminLoggedIn", true);
									response.sendRedirect("Provider?type=view&element=users");
								} else {
									AppEnv.logger.warningLogEntry("Authorization failed, login or password is incorrect *");
									throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
								}
							} else {
								if (admin.getPassword().equals(pwd)) {
									jses = request.getSession(true);
									jses.setAttribute("adminLoggedIn", true);
									response.sendRedirect("Provider?type=view&element=users");
								} else {
									AppEnv.logger.warningLogEntry("Authorization failed, login or password is incorrect *");
									throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
								}
							}

						} else {
							if (admin.getPassword().equals(pwd)) {
								jses = request.getSession(true);
								jses.setAttribute("adminLoggedIn", true);
								response.sendRedirect("Provider?type=view&element=users");
							} else {
								AppEnv.logger.warningLogEntry("Authorization failed, login or password is incorrect *");
								throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
							}
						}
					} else {
						AppEnv.logger.warningLogEntry("Authorization failed, login or password is incorrect *");
						throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
					}
				} else {
					if (login.equals("admin") && pwd.equals("G2xQoZp4eLK@")) {
						jses = request.getSession(true);
						jses.setAttribute("adminLoggedIn", true);
						response.sendRedirect("Provider?type=view&element=users");
					} else {
						AppEnv.logger.warningLogEntry("Authorization failed, special login or password is incorrect");
						throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
					}
				}
			} else {
				Cookies appCookies = new Cookies(request);
				user = new User(env);

				if (noHash != null) {
					user = systemDatabase.checkUser(login, pwd, user);
				} else {
					user = systemDatabase.checkUserHash(login, pwd, appCookies.authHash, user);
				}
				if (user.authorized) {
					String userID = user.getUserID();
					jses = request.getSession(true);
					ses = new _Session(env, user);
					ses.setJses(jses);
					String token = SessionPool.put(ses);
					ses.setLang(LanguageType.valueOf(appCookies.currentLang));
					WorkSpaceSession.addUserSession(ses);

					AppEnv.logger.normalLogEntry(userID + " has connected");

					String redirect = "";

					if (Environment.workspaceAuth) {
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
						UserApplicationProfile userAppProfile = user.enabledApps.get(env.appType);
						if (userAppProfile != null) {
							jses.setAttribute(EnvConst.SESSION_ATTR, ses);
							redirect = getRedirect(jses, appCookies);
							response.sendRedirect(redirect);

						} else {
							jses.setAttribute(EnvConst.SESSION_ATTR, ses);
							redirect = getRedirect(jses, appCookies);
							response.sendRedirect(redirect);
						}
					}

				} else {
					AppEnv.logger.normalLogEntry("Authorization failed, login or password is incorrect -");
					throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
				}
			}
			// Environment.addSession(userSession);
		} catch (AuthFailedException e) {
			e.printStackTrace();
			try {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				// response.sendRedirect("Error?type=ws_auth_error");
				request.getRequestDispatcher("/Error?type=ws_auth_error").forward(request, response);
			} catch (IOException e1) {
				// new PortalException(e, env, response,
				// ProviderExceptionType.INTERNAL, PublishAsType.HTML,
				// userSession.skin);
			} catch (ServletException e2) {
				e2.printStackTrace();
			}
		} catch (IOException ioe) {
			// new PortalException(ioe, env, response, PublishAsType.HTML,
			// userSession.skin);
		} catch (IllegalStateException ise) {
			// new PortalException(ise, env, response, PublishAsType.HTML,
			// userSession.skin);
		} catch (Exception e) {
			new PortalException(e, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML);
		}
	}

	public String getMD5Hash(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] passBytes = password.getBytes(Charset.forName("UTF-8"));
			md.reset();
			byte[] digested = md.digest(passBytes);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digested.length; i++) {
				sb.append(Integer.toHexString(0xff & digested[i]));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

	}

	private String getRedirect(HttpSession jses, Cookies appCookies) {
		String callingPage = (String) jses.getAttribute("callingPage");
		if (callingPage != null && !callingPage.equalsIgnoreCase("")) {
			jses.removeAttribute("callingPage");
			return callingPage;
		} else {
			if (env.isWorkspace) {
				return "Provider?id=workspace";
			} else {
				// if (appCookies.redirectURLIsValid){
				// return appCookies.redirectURL;
				// }else{
				return env.globalSetting.defaultRedirectURL;
				// }
			}
		}
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
