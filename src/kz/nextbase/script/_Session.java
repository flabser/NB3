package kz.nextbase.script;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LanguageType;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.caching.ICache;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.users.User;
import kz.flabs.webrule.GlobalSetting;
import kz.flabs.webrule.page.PageRule;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.mail._MailAgent;
import kz.pchelka.scheduler.IProcessInitiator;
import net.sf.saxon.s9api.SaxonApiException;

public class _Session extends _ScriptingObject implements ICache {

	private IDatabase dataBase;
	private User user;
	private SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private AppEnv env;
	private String formSesID;
	private IProcessInitiator initiator;
	private LanguageType lang;

	public _Session(AppEnv env, User user, IProcessInitiator init) {
		this.env = env;
		this.user = user;
		setInitiator(init);
		dataBase = env.getDataBase();

		this.user = user;
	}

	public _AppEntourage getAppEntourage() {
		return new _AppEntourage(this, env);
	}

	public GlobalSetting getGlobalSettings() {
		return env.globalSetting;
	}

	public LanguageType getLang() {
		return lang;

	}

	@Deprecated
	public String getCurrentUser() {
		return user.getUserID();
	}

	public String getCurrentHost() {
		return user.getSession().host;
	}

	public String getCurrentUserID() {
		return user.getUserID();
	}

	public String getCurrentDateAsString(int plusDays) {
		return dateformat.format(getDatePlusDays(plusDays));
	}

	public _ActionBar createActionBar() {
		return new _ActionBar(this);
	}

	public _ViewEntryCollectionParam createViewEntryCollectionParam() {
		return new _ViewEntryCollectionParam(this);
	}

	public Date getDatePlusDays(int plusDays) {
		Calendar date = new GregorianCalendar();
		date.setTime(new Date());
		date.add(Calendar.DAY_OF_YEAR, plusDays);
		return date.getTime();
	}

	public String getCurrentMonth() {
		Calendar date = new GregorianCalendar();
		date.setTime(new Date());
		return Integer.toString(date.get(Calendar.MONTH) + 1);
	}

	public String getCurrentYear() {
		Calendar date = new GregorianCalendar();
		date.setTime(new Date());
		return Integer.toString(date.get(Calendar.YEAR));
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

	public _Page getPage(String id, _WebFormData webFormData) throws _Exception {
		PageRule rule;
		try {
			rule = (PageRule) env.ruleProvider.getRule("page", id);
			Page page = new Page(env, user.getSession(), rule);
			return new _Page(page, webFormData);
		} catch (RuleException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Session.getPage(" + id + ")");
		} catch (QueryFormulaParserException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Session.getPage(" + id + ")");
		}
	}

	public String getLastURL() {
		return user.getSession().history.getLastEntry().URL;
	}

	@Deprecated
	public String getLastPageURL() throws _Exception {
		return user.getSession().history.getLastPageEntry().URL;
	}

	public _URL getURLOfLastPage() throws _Exception {
		return new _URL(user.getSession().history.getLastPageEntry().URL);
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return "userid=" + user.getUserID() + ", database=" + dataBase.toString();
	}

	public String getFormSesID() {
		return formSesID;
	}

	public void setFormSesID(String formSesID) {
		this.formSesID = formSesID;
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

	public PageOutcome getCachedPage(Page page, HashMap<String, String[]> formData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, QueryFormulaParserException,
	        DocumentException, DocumentAccessException, QueryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageOutcome getCachedPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, IOException,
	        SaxonApiException {
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

}
