package kz.flabs.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.LoginModeType;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.servlets.BrowserType;
import kz.flabs.servlets.Cookies;
import kz.flabs.servlets.ServletUtil;
import kz.flabs.util.PageResponse;
import kz.flabs.workspace.LoggedUser;
import kz.flabs.workspace.WorkSpaceSession;
import kz.lof.appenv.AppEnv;
import kz.lof.env.Environment;

public class UserSession implements Const {
	public User currentUser;
	public HistoryEntryCollection history;
	public QuickFilterCollection quickFilters = new QuickFilterCollection();
	public String lang, skin = "";
	public int pageSize;
	public BrowserType browserType = BrowserType.UNKNOWN;;
	public String host = "localhost";
	public String ipAddr = "";

	@Deprecated
	private HashMap<String, String> outlineCache = new HashMap<String, String>();
	@Deprecated
	private HashMap<String, Integer> currViewPage = new HashMap<String, Integer>();
	private DocID toFlash;
	private Cookies appCookies;
	private HttpSession jses;

	private HashMap<String, RunTimeParameters> currConditions = new HashMap<String, RunTimeParameters>();

	@Deprecated
	public UserSession(User user) throws UserException {
		currentUser = user;
		// currentUser.setSession(this);
		if (!currentUser.getUserID().equalsIgnoreCase(Const.sysUser)) {
			initHistory();
		}

	}

	public UserSession(User user, HttpServletRequest request) throws UserException {
		currentUser = user;
		// currentUser.setSession(this);
		appCookies = new Cookies(request);
		lang = appCookies.currentLang;
		skin = appCookies.currentSkin;
		pageSize = appCookies.pageSize;
		if (!currentUser.getUserID().equalsIgnoreCase("anonymous")) {
			initHistory();
		}
		browserType = getBrowserType(request);
		host = getHost(request);
		ipAddr = ServletUtil.getClientIpAddr(request);
	}

	public UserSession(User user, HttpServletRequest request, HttpServletResponse response, boolean saveToken, HttpSession jses) throws UserException {
		this.jses = jses;
		currentUser = user;
		// currentUser.setSession(this);
		appCookies = new Cookies(request);
		lang = appCookies.currentLang;
		skin = appCookies.currentSkin;
		pageSize = appCookies.pageSize;
		if (!currentUser.getUserID().equalsIgnoreCase(Const.sysUser)) {
			initHistory();
		}

		if (Environment.workspaceAuth) {
			String wToken = WorkSpaceSession.addUserSession(currentUser);
			if (saveToken) {
				Cookie wLoginCook = new Cookie("wauth", wToken);
				wLoginCook.setMaxAge(604800);
				wLoginCook.setPath("/");
				response.addCookie(wLoginCook);
			}
		} else {
			if (saveToken) {
				Cookie loginCook = new Cookie("auth", Integer.toString(user.getHash()));
				loginCook.setMaxAge(604800);
				response.addCookie(loginCook);
			}
		}
		browserType = getBrowserType(request);
		host = getHost(request);
		ipAddr = ServletUtil.getClientIpAddr(request);
	}

	public UserSession(ServletContext context, HttpServletRequest request, HttpServletResponse response, HttpSession jses)
	        throws AuthFailedException, UserException {
		this.jses = jses;
		appCookies = new Cookies(request);
		lang = appCookies.currentLang;
		skin = appCookies.currentSkin;
		pageSize = appCookies.pageSize;

		AppEnv env = (AppEnv) context.getAttribute("portalenv");
		ISystemDatabase systemDatabase = DatabaseFactory.getSysDatabase();

		if (Environment.workspaceAuth && appCookies.wAuthCookiesIsValid) {
			String token = appCookies.wAuthHash;
			LoggedUser lu = WorkSpaceSession.getLoggeedUser(token);
			if (lu != null) {
				currentUser = new User(env);
				currentUser = systemDatabase.checkUser(lu.getLogin(), lu.getPwd(), appCookies.authHash, currentUser);
				if (currentUser.authorized) {
					WorkSpaceSession.addUserSession(currentUser);
				} else {
					AppEnv.logger.infoLogEntry("Authorization failed, login or password is incorrect (by workspace authorization)");
					throw new AuthFailedException(AuthFailedExceptionType.PASSWORD_INCORRECT, lu.getLogin());
				}
			} else {
				throw new AuthFailedException(AuthFailedExceptionType.NO_USER_SESSION, "");
			}
			initHistory();
		} else {
			int userHash = Integer.parseInt(appCookies.authHash);
			if (userHash == 0) {
				AppEnv.logger.infoLogEntry("Authorization failed, login or password is incorrect/");
				throw new AuthFailedException(AuthFailedExceptionType.NO_USER_SESSION, "");
			} else {
				currentUser = new User(userHash, env);
				UserApplicationProfile userAppProfile = currentUser.enabledApps.get(env.appType);
				if (userAppProfile != null && userAppProfile.loginMod == LoginModeType.LOGIN_AND_QUESTION) {
					throw new AuthFailedException(AuthFailedExceptionType.UNANSWERED_QUESTION, "");
				}
				initHistory();
			}
		}
		// currentUser.setSession(this);
		browserType = getBrowserType(request);
		host = getHost(request);
		ipAddr = ServletUtil.getClientIpAddr(request);
	}

	public void setObject(String name, StringBuffer obj) {
		HashMap<String, StringBuffer> cache = null;
		if (jses != null) {
			cache = (HashMap<String, StringBuffer>) jses.getAttribute("cache");
		}
		if (cache == null) {
			cache = new HashMap<>();
		}
		cache.put(name, obj);
		if (jses != null) {
			jses.setAttribute("cache", cache);
		}

	}

	public Object getObject(String name) {
		try {
			HashMap<String, StringBuffer> cache = (HashMap<String, StringBuffer>) jses.getAttribute("cache");
			return cache.get(name);
		} catch (Exception e) {
			return null;
		}
	}

	@Deprecated
	public void setCurrentPage(String view, int page) {
		currViewPage.put(view, page);
	}

	public void setLang(String lang, HttpServletResponse response) {
		this.lang = lang;
		Cookie cpCookie = new Cookie("lang", lang);
		cpCookie.setMaxAge(99999);
		cpCookie.setPath("/");
		response.addCookie(cpCookie);
	}

	public void setPageSize(String size, HttpServletResponse response) {
		try {
			pageSize = Integer.parseInt(size);
		} catch (NumberFormatException e) {
			pageSize = 30;
			size = "30";
		}

		Cookie cpCookie = new Cookie("pagesize", size);
		cpCookie.setMaxAge(999991);
		response.addCookie(cpCookie);
	}

	public int getCurrentPage(String view) {
		Integer page = currViewPage.get(view);
		if (page == null) {
			page = 1;
		}
		return page;
	}

	public void setOutline(String id, String lang, String content) {
		outlineCache.put(id + lang, content);
	}

	public String getOutline(String id, String lang) {
		return outlineCache.get(id + lang);
	}

	@Deprecated
	public void setFlashViewEntry(DocID flashDoc) {
		toFlash = flashDoc;
	}

	public void setFlash(DocID flashDoc) {
		toFlash = flashDoc;
	}

	public DocID getFlashDoc() {
		try {
			return toFlash;
		} finally {
			toFlash = null;
		}
	}

	public void addHistoryEntry(String type, String url, String title) throws UserException {
		HistoryEntry entry = new HistoryEntry(type, url, title);
		history.add(entry);
	}

	public Document getAsDocument(IDatabase db) {
		return null;

		/*
		 * ISystemDatabase sysDb = DatabaseFactory.getSysDatabase();
		 * sysDb.reloadUserData(currentUser, currentUser.getUserID()); if
		 * (currentUser.getAppUser() != null) { Employer emp =
		 * currentUser.getAppUser(); doc = emp; } else { doc = new
		 * Employer(db.getStructure()); } doc.addField("userid",
		 * currentUser.getUserID(), FieldType.TEXT); doc.addField("email",
		 * currentUser.getEmail(), FieldType.TEXT);
		 * doc.addField("instmsgaddress", currentUser.getInstMsgAddress(),
		 * FieldType.TEXT); doc.addField("instmsgstatus",
		 * Boolean.toString(currentUser.isInstMsgOnLine()), FieldType.TEXT);
		 * doc.addField("lang", lang, FieldType.TEXT); doc.setSkin(skin);
		 * doc.setCountDocInView(pageSize); UserGroup replaceGroup =
		 * db.getStructure().getGroup("[" + doc.getUserID() + "]",
		 * Const.sysGroupAsSet, Const.sysUser); if (replaceGroup != null) {
		 * doc.addListField("replacer", new
		 * ArrayList<String>(replaceGroup.getMembers())); } doc.isValid = true;
		 * doc.editMode = EDITMODE_EDIT; doc.setNewDoc(false);
		 */

	}

	private static BrowserType getBrowserType(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		if (userAgent == null || userAgent.length() == 0) {
			return BrowserType.APPLICATION;
		}
		// Server.logger.verboseLogEntry("userAgent=" + userAgent);
		if (userAgent.indexOf("MSIE") != -1) {
			return BrowserType.IE;
		} else if (userAgent.indexOf("Firefox") != -1 && userAgent.indexOf("Android") == -1) {
			return BrowserType.FIREFOX;
		} else if (userAgent.indexOf("Chrome") != -1 && userAgent.indexOf("Android") == -1) {
			return BrowserType.CHROME;
		} else if (userAgent.indexOf("Opera") != -1 && userAgent.indexOf("Android") == -1) {
			return BrowserType.OPERA;
		} else if (userAgent.indexOf("iPad") != -1) {
			return BrowserType.IPAD_SAFARI;
		} else if (userAgent.indexOf("iPhone") != -1) {
			return BrowserType.IPAD_SAFARI;
		} else if (userAgent.indexOf("Android") != -1) {
			return BrowserType.ANDROID;
		} else if (userAgent.indexOf("P1000") != -1) {
			return BrowserType.GALAXY_TAB_SAFARI;
		} else if (userAgent.indexOf("Safari") != -1) {
			return BrowserType.SAFARI;
		} else if (userAgent.indexOf("CFNetwork") != -1) {
			return BrowserType.APPLICATION;
		} else {
			return BrowserType.UNKNOWN;
		}
	}

	private String getHost(HttpServletRequest request) {
		int port = request.getServerPort();
		if (port == 80) {
			return request.getServerName();
		} else {
			return request.getServerName() + ":" + Integer.toString(port);
		}
	}

	public class QuickFilterCollection {
		private HashSet<Filter> quickFilters = new HashSet<Filter>();

		public void add(Filter filter) {
			quickFilters.add(filter);
		}

		public void remove(Filter filter) {
			quickFilters.remove(filter);
		}

		public Filter getQuickFilter(String name) {
			if (quickFilters.isEmpty()) {
				return null;
			}
			for (Filter f : quickFilters) {
				if (f.getName().equalsIgnoreCase(name)) {
					return f;
				}
			}
			return null;
		}

		public Collection<Filter> getQuickFilters() {
			return quickFilters;
		}

	}

	public class HistoryEntryCollection {
		// type of collection has been changed from linked list to
		// LinkedBlockingDeque for better thread safe
		private LinkedBlockingDeque<HistoryEntry> history = new LinkedBlockingDeque<HistoryEntry>();
		private LinkedBlockingDeque<HistoryEntry> pageHistory = new LinkedBlockingDeque<HistoryEntry>();

		public void add(HistoryEntry entry) throws UserException {
			if (history.size() == 0 || !history.getLast().equals(entry)) {
				history.add(entry);
				if (entry.isPageURL) {
					pageHistory.add(entry);
				}
			}

			if (history.size() > 10) {
				history.removeFirst();
				try {
					pageHistory.removeFirst();
				} catch (NoSuchElementException e) {

				}
			}

		}

		public boolean remove(Collection<Filter> col) throws DocumentException {
			if (col.isEmpty()) {
				return false;
			}
			ArrayList<HistoryEntry> entries = new ArrayList<HistoryEntry>();
			for (Filter f : col) {
				entries.add(new HistoryEntry("outline", f.getURL(), "Home page"));
			}

			boolean result = history.removeAll(entries);
			if (history.isEmpty()) {
				/*
				 * HistoryEntry redirect = new HistoryEntry("outline", homeURL,
				 * "Home page"); history.add(redirect);
				 */
			}
			return result;
		}

		public Collection<HistoryEntry> getEntries() {
			return history;
		}

		public HistoryEntry getLastEntry() {
			try {
				return history.getLast();
			} catch (Exception e) {
				return new HistoryEntry("view", currentUser.getAppEnv().globalSetting.defaultRedirectURL, "");
			}
		}

		public HistoryEntry getLastPageEntry() {
			try {
				HistoryEntry url = pageHistory.getLast();
				return url;
			} catch (Exception e) {
				return new HistoryEntry("page", currentUser.getAppEnv().globalSetting.defaultRedirectURL, "");
			}
		}

		@Override
		public String toString() {
			String v = "";
			for (HistoryEntry entry : history) {
				v += entry.toString() + "\n";
			}
			return v;
		}

	}

	public class HistoryEntry {
		public String URL;
		public String URLforXML;
		public String title;
		public String type;
		public Date time;
		public boolean isPageURL;

		HistoryEntry(String type, String url, String title) {
			URL = url;
			URLforXML = url;
			this.title = title;
			this.type = type;
			time = new Date();
			isPageURL = isPage(url);
		}

		@Override
		public boolean equals(Object obj) {
			HistoryEntry entry = (HistoryEntry) obj;
			return entry.URLforXML.equalsIgnoreCase(URLforXML);
		}

		@Override
		public int hashCode() {
			return this.URLforXML.hashCode();
		}

		@Override
		public String toString() {
			return URLforXML;
		}

		private boolean isPage(String url) {
			return url.indexOf("type=page") > -1;
		}
	}

	private void initHistory() throws UserException {
		history = new HistoryEntryCollection();
	}

	public String getCacheInfo() {
		String res = "";
		try {
			HashMap<String, StringBuffer> cache = (HashMap<String, StringBuffer>) jses.getAttribute("cache");
			if (cache != null) {
				for (String c : cache.keySet()) {
					res = res + "," + c;
				}
			}
		} catch (IllegalStateException e) {

		}

		return res;
	}

	public void setRuntimeConditions(String key, RunTimeParameters value) {
		RunTimeParameters old_value = currConditions.get(key);
		if (value.filtersMap.size() > 0) {
			if (old_value != null) {
				old_value.filtersMap.putAll(value.filtersMap);
				value.filtersMap = old_value.filtersMap;
				value.filters.addAll(old_value.filters);
				for (Map.Entry<String, RunTimeParameters.Filter> f : value.filtersMap.entrySet()) {
					if ("".equalsIgnoreCase(f.getValue().keyWord)) {
						old_value.filtersMap.remove(f.getKey());
						old_value.filters.remove(f.getValue());
					}
				}
			}
		}

		if (value.sortingMap.size() > 0) {

			if (old_value != null) {
				old_value.sortingMap.putAll(value.sortingMap);
				old_value.sorting.addAll(value.sorting);
			}
		}
		currConditions.put(key, value);
	}

	public HashMap<String, RunTimeParameters> getRuntimeConditions() {
		return currConditions;
	}

	@Override
	public String toString() {
		return jses.getId();
	}

	public void setPageObject(String name, PageResponse obj) {
		HashMap<String, PageResponse> cache = null;
		if (jses != null) {
			cache = (HashMap<String, PageResponse>) jses.getAttribute("cache");
		}
		if (cache == null) {
			cache = new HashMap<>();
		}
		cache.put(name, obj);
		if (jses != null) {
			jses.setAttribute("cache", cache);
		}

	}
}
