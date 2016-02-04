package kz.flabs.scriptprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kz.flabs.dataengine.jpa.DAO;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.nextbase.script._IPOJOObject;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._URL;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;

public class ScriptEvent {
	protected Vocabulary vocabulary;
	protected String redirectURL = "";
	protected ArrayList<_IXMLContent> toPublishElement = new ArrayList<_IXMLContent>();
	protected _Session ses;
	protected LanguageType lang;

	public String getTmpDirPath() {
		return Environment.tmpDir;
	}

	public String getWord(String word, Vocabulary vocabulary, String lang) {
		try {
			return vocabulary.getSentenceCaption(word, lang).word;
		} catch (Exception e) {
			return word.toString();
		}
	}

	protected _ActionBar getSimpleActionBar(_Session session, String type, LanguageType lang) {
		_ActionBar actionBar = new _ActionBar(session);
		_Action newDocAction = new _Action(getLocalizedWord("new_", lang), getLocalizedWord("add_new_", lang), "new_" + type);
		newDocAction.setURL("Provider?id=" + type + "&key=");
		actionBar.addAction(newDocAction);
		actionBar.addAction(new _Action(getLocalizedWord("del_document", lang), getLocalizedWord("del_document", lang), _ActionType.DELETE_DOCUMENT));
		return actionBar;
	}

	protected _POJOListWrapper getViewPage(DAO<? extends _IPOJOObject, UUID> dao, _WebFormData formData) {
		int pageNum = 1;
		int pageSize = dao.getSession().pageSize;
		if (formData.containsField("page")) {
			pageNum = formData.getNumberValueSilently("page", pageNum);
		}
		long count = dao.getCount();
		int maxPage = RuntimeObjUtil.countMaxPage(count, pageSize);
		if (pageNum == 0) {
			pageNum = maxPage;
		}
		int startRec = RuntimeObjUtil.calcStartEntry(pageNum, pageSize);
		List<? extends _IPOJOObject> list = dao.findAll(startRec, pageSize);
		return new _POJOListWrapper(list, maxPage, count, pageNum, lang);

	}

	public void println(Object text) {
		System.out.println(text.toString());
	}

	public void log(String text) {
		Server.logger.normalLogEntry(text);
	}

	/*
	 * public void publishElement(String spot, String launcher, _AJAXHandler
	 * value, boolean async, _JSONTemplate template) { _JSONHandler jsHandler =
	 * new _JSONHandler(spot, launcher, value, template); UserSession
	 * userSession = ses.getUser().getSession();
	 * userSession.addDynmaicClass(jsHandler.id, jsHandler.getInstance());
	 * toPublishElement.add(jsHandler); }
	 * 
	 * public void publishElement(String id, String spot, String launcher,
	 * _AJAXHandler value, boolean async, _JSONTemplate template) { _JSONHandler
	 * jsHandler = new _JSONHandler(id, spot, launcher, value, "", template);
	 * UserSession userSession = ses.getUser().getSession();
	 * userSession.addDynmaicClass(jsHandler.id, jsHandler.getInstance());
	 * toPublishElement.add(jsHandler); }
	 */

	public String getGroovyError(StackTraceElement stack[]) {
		for (int i = 0; i < stack.length; i++) {
			if (stack[i].getClassName().contains(this.getClass().getName())) {
				return stack[i].getClassName() + " method=" + stack[i].getMethodName() + " > " + Integer.toString(stack[i].getLineNumber()) + "\n";
			}
		}
		return "";
	}

	@Deprecated
	public String getLocalizedWord(String word, String lang) {
		return getWord(word, vocabulary, lang);
	}

	public String getLocalizedWord(String word, LanguageType lang) {
		return getWord(word, vocabulary, lang.name());
	}

	public void setRedirectURL(_URL url) {
		redirectURL = url.toString();
	}

	public static void log(Object logText) {
		ScriptProcessor.logger.normalLogEntry(logText.toString());
	}

	public static void error(Exception e) {
		ScriptProcessor.logger.errorLogEntry(e);
	}

	@Deprecated
	public void setRedirectURL(String url) {
		redirectURL = url;
	}

	@Deprecated
	public void setRedirectPage(String page) {
		redirectURL = "Provider?type=page&element=" + page;
	}
}
