package kz.nextbase.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.jpa.IAppEntity;
import kz.flabs.localization.LanguageType;
import kz.flabs.runtimeobj.caching.ICache;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.users.User;
import kz.flabs.webrule.GlobalSetting;
import kz.lof.user.AuthModeType;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.mail._MailAgent;
import kz.pchelka.scheduler.IProcessInitiator;

public class _Session extends _ScriptingObject implements ICache {

	private IDatabase dataBase;
	private User user;
	private AppEnv env;
	private IProcessInitiator initiator;
	private LanguageType lang;
	public int pageSize = 30;
	private AuthModeType authMode;
	private ArrayList<_Session> descendants = new ArrayList<_Session>();
	private HttpSession jses;
	private HashMap<UUID, FormTransaction> formTrans = new HashMap<UUID, FormTransaction>();

	public _Session(AppEnv env, User user) {
		this.env = env;
		this.user = user;
		dataBase = env.getDataBase();
	}

	public _AppEntourage getAppEntourage() {
		return new _AppEntourage(this, env);
	}

	public GlobalSetting getGlobalSettings() {
		return env.globalSetting;
	}

	public void setLang(LanguageType lang) {
		this.lang = lang;

	}

	public LanguageType getLang() {
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

	public IProcessInitiator getInitiator() {
		return initiator;
	}

	public void setInitiator(IProcessInitiator initiator) {
		this.initiator = initiator;
	}

	public _Employer getCurrentAppUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageOutcome getCachedPage(Page page, _WebFormData formData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	public String getCacheInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	private void setPageSize(int ps) {
		pageSize = ps;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setJses(HttpSession jses) {
		this.jses = jses;
	}

	public _Session clone(AppEnv env) {
		_Session newSes = new _Session(env, user);
		newSes.authMode = AuthModeType.LOGIN_THROUGH_TOKEN;
		newSes.setLang(lang);
		newSes.setPageSize(pageSize);
		descendants.add(newSes);
		return newSes;
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
		UUID id = entity.getId();
		if (id != null) {
			FormTransaction ft = formTrans.get(id);
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

}
