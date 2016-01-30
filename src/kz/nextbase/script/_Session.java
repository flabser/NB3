package kz.nextbase.script;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.users.User;
import kz.flabs.webrule.GlobalSetting;
import kz.flabs.webrule.page.PageRule;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.coordination._Block;
import kz.nextbase.script.coordination._BlockCollection;
import kz.nextbase.script.coordination._Coordinator;
import kz.nextbase.script.mail._InstMessengerAgent;
import kz.nextbase.script.mail._MailAgent;
import kz.nextbase.script.struct._Employer;
import kz.nextbase.script.struct._EmployerCollection;
import kz.nextbase.script.struct._Structure;
import kz.pchelka.log.ILogger;
import kz.pchelka.scheduler.IProcessInitiator;


public class _Session extends _ScriptingObject {

	private _Database db;
	private IDatabase dataBase;
	private User user;
	private SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private AppEnv env;
	private String formSesID;
	private IProcessInitiator initiator;
	private _Document documentInConext;

	public _Session(AppEnv env, User user, IProcessInitiator init) {
		this.env = env;
		this.user = user;
		setInitiator(init);
		dataBase = env.getDataBase();
		if (dataBase != null) this.db = new _Database(dataBase, user.getUserID(), this);
		this.user = user;
	}

	public _AppEntourage getAppEntourage() {
		return new _AppEntourage(this, env);
	}

	public GlobalSetting getGlobalSettings() {
		return env.globalSetting;
	}

	public String getCurrentDateAsString() {
		return dateformat.format(new Date());
	}

	@Deprecated
	public ILogger getLogger() {
		return env.logger;
	}

	@Deprecated
	public String getCurrentUser() {
		return user.getUserID();
	}

	public String getAppURL() {
		return user.getSession().host + "/" + user.env.appType;
	}

	public _Employer getCurrentAppUser() {
		return new _Employer(user.getAppUser(), this);
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

	public _EmployerCollection createEmployerCollection(String[] e) {
		return new _EmployerCollection(this.dataBase, this, e);
	}

	public _BlockCollection createBlockCollection() {
		return new _BlockCollection(this);
	}

	public _Block createBlock(String data) throws _Exception {
		return new _Block(data, this);
	}

	public _Coordinator createCoordinator() {
		return new _Coordinator(this.dataBase);
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

	public _Database getCurrentDatabase() {
		return db;
	}

	public _Structure getStructure() {
		return new _Structure(dataBase, user);
	}

	public _UserActivity getUserActivity() {
		return new _UserActivity(dataBase, user);
	}

	public _MailAgent getMailAgent() {
		return new _MailAgent(this);
	}

	public _InstMessengerAgent getInstMessengerAgent() {
		return new _InstMessengerAgent();
	}

	public _Page getPage(String id, _WebFormData webFormData) throws _Exception {
		PageRule rule;
		try {
			rule = (PageRule) env.ruleProvider.getRule("page", id);
			Page page = new Page(env, user.getSession(), rule);
			return new _Page(page, webFormData);
		} catch (RuleException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Session.getPage("
					+ id + ")");
		} catch (QueryFormulaParserException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Session.getPage("
					+ id + ")");
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

	public String toString() {
		return "userid=" + user.getUserID() + ", database=" + dataBase.toString();
	}

	public String getFormSesID() {
		return formSesID;
	}

	public void setFormSesID(String formSesID) {
		this.formSesID = formSesID;
	}

	@Deprecated
	public Set <DocID> getExpandedDocuments() {
		return user.getSession().expandedThread;
	}

	public Set <String> getExpandedThread() {
		return new HashSet <String>();
	}

	public IProcessInitiator getInitiator() {
		return initiator;
	}

	public void setInitiator(IProcessInitiator initiator) {
		this.initiator = initiator;
	}

	public _Document getDocumentInConext() {
		return documentInConext;
	}

	public void setDocumentInConext(_Document documentInConext) {
		this.documentInConext = documentInConext;
	}

	public void setFlash(_Document doc) {
		user.getSession().setFlashViewEntry(new DocID(doc.getDocID(), doc.getDocType()));
	}

}
