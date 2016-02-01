package kz.flabs.servlets;

import java.io.IOException;
import java.io.PrintWriter;
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
import kz.flabs.dataengine.h2.LoginModeType;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.PortalException;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.AuthFailedExceptionType;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.util.PageResponse;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.pchelka.env.Environment;

import org.apache.catalina.realm.RealmBase;

public class Login extends HttpServlet implements Const {
	private static final long serialVersionUID = 1L;
	private AppEnv env;
	private HashMap<String, UserSession> unauthorizedUserSessions = new HashMap<String, UserSession>();
	private static final int numOfAttempt = 5;

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
		try {
			String login = request.getParameter("login");
			String pwd = request.getParameter("pwd");
			String noAuth = request.getParameter("noauth");
			String qID = request.getParameter("qid");
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
				// AppEnv.logger.warningLogEntry("Authorization failed, special
				// login or password is incorrect");
				Cookies appCookies = new Cookies(request);
				if (qID == null) {
					user = new User(env);

					if (noHash != null) {
						user = systemDatabase.checkUser(login, pwd, user);
					} else {
						user = systemDatabase.checkUserHash(login, pwd, appCookies.authHash, user);
					}
					if (user.authorized) {
						String userID = user.getUserID();

						boolean saveToken = true;
						if (noAuth != null && noAuth.equals("1")) {
							saveToken = false;
						}

						jses = request.getSession(true);

						userSession = new UserSession(user, request, response, saveToken, jses);

						AppEnv.logger.normalLogEntry(userID + " has connected");

						String redirect = "";
						if (userSession.browserType == BrowserType.APPLICATION) {
							jses.setAttribute("usersession", userSession);
							PageResponse resp = new PageResponse(ResponseType.AUTHENTICATION, true);
							response.setContentType("text/xml;charset=utf-8");
							PrintWriter out = response.getWriter();
							out.println(resp.toCompleteXML());
							out.close();
						} else {
							if (Environment.workspaceAuth) {
								jses.setAttribute("usersession", userSession);
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
									if (userAppProfile.loginMod == LoginModeType.LOGIN_AND_REDIRECT) {
										jses.setAttribute("usersession", userSession);
										redirect = getRedirect(jses, appCookies);
										response.sendRedirect(redirect);
									} else {
										PageResponse xmlResult = new PageResponse(ResponseType.AUTHENTICATION);
										if (userAppProfile.loginMod == LoginModeType.LOGIN_AND_QUESTION) {
											if (user.authorizedByHash) {
												xmlResult.setMessage("authorized by hash", "warn");
												xmlResult.setResponseStatus(false);
												response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
											} else {
												String qid = Util.generateRandomAsText();
												unauthorizedUserSessions.put(qid, userSession);
												UserApplicationProfile.QuestionAnswer qa = userAppProfile.getSomeQuestion();
												jses.setAttribute("up", userAppProfile);
												jses.setAttribute("qa", qa);
												xmlResult.setResponseType(ResponseType.SUPPLY_LOGIN_QUESTION);
												xmlResult.setMessage(qid, "qid");
												xmlResult.addMessage(qa.controlQuestion, "qq");
												xmlResult.addMessage(Integer.toString(getQuestionAttempCount(jses)), "attempt_count");
											}
										} else if (userAppProfile.loginMod == LoginModeType.JUST_LOGIN) {
											jses.setAttribute("usersession", userSession);
											redirect = getRedirect(jses, appCookies);

										}
										StringBuffer output = new StringBuffer(100);
										output.append(xmlResult.toXML());
										response.setContentType("text/xml;charset=UTF-8");
										ProviderOutput po = new ProviderOutput("login", "", output, request, response, userSession, jses, "", false);
										String outputContent = po.getStandartUTF8Output();
										PrintWriter out = response.getWriter();
										out.println(outputContent);
										out.close();
									}
								} else {
									jses.setAttribute("usersession", userSession);
									redirect = getRedirect(jses, appCookies);
									response.sendRedirect(redirect);
								}
							}
						}
					} else {
						userSession = new UserSession(user, request);
						if (userSession.browserType == BrowserType.APPLICATION) {
							PageResponse resp = new PageResponse(ResponseType.AUTHENTICATION, false);
							// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("text/xml;charset=UTF-8");
							PrintWriter out = response.getWriter();
							System.out.println("Login " + resp.toCompleteXML());
							out.println(resp.toCompleteXML());
							out.close();
						} else {
							AppEnv.logger.normalLogEntry("Authorization failed, login or password is incorrect -");
							throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, login);
						}
					}
				} else {
					jses = request.getSession(false);
					PageResponse xmlResp = new PageResponse(ResponseType.AUTHENTICATION);
					if (jses != null && unauthorizedUserSessions.containsKey(qID)) {
						UserApplicationProfile.QuestionAnswer qa = (UserApplicationProfile.QuestionAnswer) jses.getAttribute("qa");
						userSession = unauthorizedUserSessions.get(qID);
						String answer = request.getParameter("answer").trim();
						if (answer.equalsIgnoreCase(qa.answer)) {
							jses.setAttribute("usersession", userSession);
							xmlResp.setResponseStatus(true);
							xmlResp.addMessage(getRedirect(jses, appCookies));
							jses.removeAttribute("qa");
						} else {
							// AppEnv.logger.warningLogEntry("Answer wrong");
							xmlResp.setResponseStatus(false);
							xmlResp.setMessage("answer wrong", "warn");
							int ac = getQuestionAttempCount(jses);
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							if (ac < 1) {
								userSession = null;
								jses.invalidate();
								unauthorizedUserSessions.remove(qID);
								xmlResp.setMessage("count of attempts is over", "warn");
							} else {
								qa = ((UserApplicationProfile) jses.getAttribute("up")).getSomeQuestion();
								jses.setAttribute("qa", qa);
								String qid = Util.generateRandomAsText();
								xmlResp.setResponseType(ResponseType.SUPPLY_LOGIN_QUESTION);
								xmlResp.addMessage(qid);
								xmlResp.addMessage(qa.controlQuestion);
							}
							xmlResp.addMessage(Integer.toString(ac), "attempt_count");
						}
					} else {
						xmlResp.setMessage("login session has been invalidated", "warn");
						xmlResp.setResponseStatus(false);
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					}
					response.setContentType("text/xml;charset=UTF-8");
					PrintWriter out = response.getWriter();
					out.println(xmlResp.toCompleteXML());
					out.close();

				}
			}
			Environment.addSession(userSession);
		} catch (AuthFailedException e) {
			try {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				// response.sendRedirect("Error?type=ws_auth_error");
				request.getRequestDispatcher("/Error?type=ws_auth_error").forward(request, response);
			} catch (IOException e1) {
				new PortalException(e, env, response, ProviderExceptionType.INTERNAL, PublishAsType.HTML, userSession.skin);
			} catch (ServletException e2) {
				e2.printStackTrace();
			}
		} catch (IOException ioe) {
			new PortalException(ioe, env, response, PublishAsType.HTML, userSession.skin);
		} catch (IllegalStateException ise) {
			new PortalException(ise, env, response, PublishAsType.HTML, userSession.skin);
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
				return "Provider?type=page&id=workspace";
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

	private int getQuestionAttempCount(HttpSession jses) {
		try {
			String countAsString = (String) jses.getAttribute("qa_count");
			int count = Integer.parseInt(countAsString) - 1;
			countAsString = Integer.toString(count);
			jses.setAttribute("qa_count", countAsString);
			return count;
		} catch (Exception e) {
			jses.setAttribute("qa_count", Integer.toString(numOfAttempt));
			return numOfAttempt;
		}

	}

}
