package kz.lof.scripting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.users.User;
import kz.flabs.webrule.GlobalSetting;
import kz.lof.appenv.AppEnv;
import kz.lof.caching.PageCacheAdapter;
import kz.lof.dataengine.jpa.IAppEntity;
import kz.lof.env.EnvConst;
import kz.lof.localization.LanguageCode;
import kz.lof.user.AuthModeType;
import kz.nextbase.script._AppEntourage;
import kz.nextbase.script._UserActivity;
import kz.nextbase.script._ViewEntryCollectionParam;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.mail._MailAgent;

public class _Session extends PageCacheAdapter {
	private IDatabase dataBase;
	private User user;
	private AppEnv env;
	private LanguageCode lang;
	public int pageSize = 30;
	private AuthModeType authMode;
	private ArrayList<_Session> descendants = new ArrayList<_Session>();
	private _Session parent;
	private HttpSession jses;
	private HashMap<UUID, FormTransaction> formTrans = new HashMap<UUID, FormTransaction>();
	private Map<String, Object> valuesMap = new HashMap<String, Object>();
	private Map<String, PersistValue> persistValuesMap = new HashMap<String, PersistValue>();

	public _Session(AppEnv env, User user) {
		this.env = env;
		this.user = user;
		dataBase = env.getDataBase();
	}

	public AppEnv getAppEnv() {
		return env;
	}

	public _AppEntourage getAppEntourage() {
		return new _AppEntourage(this, env);
	}

	public GlobalSetting getGlobalSettings() {
		return env.globalSetting;
	}

	public void setLang(LanguageCode lang) {
		// System.out.println("set lang" + lang);
		if (this.lang != lang) {
			this.lang = lang;
			if (authMode == AuthModeType.LOGIN_THROUGH_TOKEN) {
				parent.setLang(lang);
			} else {
				for (_Session childSes : descendants) {
					childSes.setLang(lang);
				}
				PersistValue pv = new PersistValue(EnvConst.LANG_COOKIE_NAME, lang.name());
				persistValuesMap.put(EnvConst.LANG_COOKIE_NAME, pv);
			}
		}
	}

	public LanguageCode getLang() {
		return lang;
	}

	public _ActionBar createActionBar() {
		return new _ActionBar(this);
	}

	public _ViewEntryCollectionParam createViewEntryCollectionParam() {
		return new _ViewEntryCollectionParam(this);
	}

	public IDatabase getCurrentDatabase() {
		return dataBase;
	}

	public _UserActivity getUserActivity() {
		return new _UserActivity(dataBase, user);
	}

	public _MailAgent getMailAgent() {
		return new _MailAgent(this);
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return "userid=" + user.getUserID() + ", database=" + dataBase.toString();
	}

	public Set<String> getExpandedThread() {
		return new HashSet<String>();
	}

	private void setPageSize(int ps) {
		pageSize = ps;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Map<String, PersistValue> getPersistValuesMap() {
		return persistValuesMap;
	}

	public void setJses(HttpSession jses) {
		this.jses = jses;
	}

	public _Session clone(AppEnv env) {
		_Session newSes = new _Session(env, user);
		newSes.parent = this;
		newSes.authMode = AuthModeType.LOGIN_THROUGH_TOKEN;
		newSes.setLang(lang);
		newSes.setPageSize(pageSize);
		newSes.setValuesMap(valuesMap);

		descendants.add(newSes);
		return newSes;
	}

	private void setValuesMap(Map<String, Object> vm) {
		this.valuesMap = vm;

	}

	public void setAttribute(String varName, Object fn) {
		valuesMap.put(varName, fn);
	}

	public Object getAttribute(String varName) {
		return valuesMap.get(varName);

	}

	public void removeAttribute(String varName) {
		valuesMap.remove(varName);
	}

	public void addFormTransaction(IAppEntity entity, String referrer) {
		UUID id = entity.getId();
		if (id != null) {
			FormTransaction ft = new FormTransaction();
			ft.setCreated(new Date());
			ft.setRefrerrer(referrer);
			formTrans.put(id, ft);
		}
	}

	public String getTransactRedirect(IAppEntity entity) {

		FormTransaction ft = formTrans.get(entity.getId());
		if (ft != null) {
			ft.isExpired = true;
			return ft.getRefrerrer();
		} else {
			return "Provider?id=" + entity.getDefaultViewName() + "&page=0";
		}

	}

	// TODO Need a ft flusher
	class FormTransaction {
		public boolean isExpired;

		private Date created;
		private String refrerrer;

		public Date getCreated() {
			return created;
		}

		public void setCreated(Date created) {
			this.created = created;
		}

		public String getRefrerrer() {
			return refrerrer;
		}

		public void setRefrerrer(String refrerrer) {
			this.refrerrer = refrerrer;
		}
	}

	public class PersistValue {
		private String name;
		private String value;

		public PersistValue(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public Cookie getCookie() {
			Cookie c = new Cookie(name, value);
			c.setMaxAge(-1);
			c.setPath("/");
			return c;

		}

	}
}
