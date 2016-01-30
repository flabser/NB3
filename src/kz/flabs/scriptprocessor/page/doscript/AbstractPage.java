package kz.flabs.scriptprocessor.page.doscript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import kz.flabs.dataengine.jpa.DAO;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.scriptprocessor.Msg;
import kz.flabs.scriptprocessor.ScriptEvent;
import kz.flabs.scriptprocessor.ScriptProcessorUtil;
import kz.flabs.servlets.SignalType;
import kz.flabs.servlets.pojo.Outcome;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.flabs.util.XMLResponse;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IPOJOObject;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._Tag;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script._XMLDocument;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;
import kz.nextbase.script.reports._ExportManager;
import kz.pchelka.server.Server;

import org.apache.http.HttpStatus;

public abstract class AbstractPage extends ScriptEvent implements IPageScript {
	public ArrayList<Msg> messages = new ArrayList<Msg>();

	private LanguageType lang;
	private _WebFormData formData;
	private XMLResponse xmlResp = new XMLResponse(ResponseType.RESULT_OF_PAGE_SCRIPT);
	@Deprecated
	private SignalType signal;
	private ArrayList<_IXMLContent> xml = new ArrayList<_IXMLContent>();
	private int httpStatus = HttpStatus.SC_OK;

	@Override
	public void setSession(_Session ses) {
		this.ses = ses;
	}

	@Deprecated
	public void localizedMsgBox(String m) {
		messages.add(new Msg(vocabulary.getWord(m, lang.name())[0], m));
	}

	public void addMsg(String m) {
		messages.add(new Msg(m, m));
	}

	public <T extends Enum<?>> String[] getLocalizedWord(T[] enumObj, String lang) {
		String[] array = new String[enumObj.length];
		try {
			for (int i = 0; i < enumObj.length; i++) {
				array[i] = vocabulary.getSentenceCaption(enumObj[i].name(), lang).word;
			}

			return array;

		} catch (Exception e) {
			return array;
		}
	}

	@Override
	public void setFormData(_WebFormData formData) {
		this.formData = formData;
	}

	@Override
	public void setCurrentLang(Vocabulary vocabulary, String lang) {
		this.lang = LanguageType.valueOf(lang);
		this.vocabulary = vocabulary;
	}

	protected void setContent(Collection<_IXMLContent> documents) {
		xml.addAll(documents);
	}

	/*
	 * protected void setContent(List list) { for (Object element : list) {
	 * _IXMLContent c = new _IXMLContent() {
	 * 
	 * @Override public String toXML() throws _Exception { return "<entry>" +
	 * element.toString() + "</entry>"; } }; xml.add(c); }
	 * 
	 * }
	 */

	protected void setContent(_IXMLContent document) {
		xml.add(document);
	}

	protected void setContent(_IPOJOObject pojo) {
		setContent(new _POJOObjectWrapper(pojo, lang));

	}

	protected void setBadRequest() {
		httpStatus = HttpStatus.SC_BAD_REQUEST;
	}

	protected _ActionBar getSimpleActionBar(_Session session, String type, LanguageType lang) {
		_ActionBar actionBar = new _ActionBar(session);
		_Action newDocAction = new _Action(getLocalizedWord("new_", lang), getLocalizedWord("add_new_", lang), "new_" + type);
		newDocAction.setURL("Provider?id=" + type + "&key=");
		actionBar.addAction(newDocAction);
		actionBar.addAction(new _Action(getLocalizedWord("del_document", lang), getLocalizedWord("del_document", lang), _ActionType.DELETE_DOCUMENT));
		return actionBar;
	}

	protected _IXMLContent getViewPage(DAO<? extends _IPOJOObject, UUID> dao, _WebFormData formData) {
		int pageNum = 1;
		int pageSize = dao.user.getSession().pageSize;
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

	public void println(Exception e) {
		String errText = e.toString();
		System.out.println(errText);
		_Tag tag = new _Tag("error", errText);
		tag.addTag(ScriptProcessorUtil.getScriptError(e.getStackTrace()));
		_XMLDocument xml = new _XMLDocument(tag);
		setContent(xml);
	}

	public void println(Object text) {
		System.out.println(text.toString());
	}

	public void log(String text) {
		Server.logger.normalLogEntry(text);
	}

	public void showFile(String filePath, String fileName) {
		xmlResp.type = ResponseType.SHOW_FILE_AFTER_HANDLER_FINISHED;
		xmlResp.addMessage(filePath, "filepath");
		xmlResp.addMessage(fileName, "originalname");
	}

	public void showFile(_ExportManager attachment) {
		xmlResp.type = ResponseType.SHOW_FILE_AFTER_HANDLER_FINISHED;
		xmlResp.addMessage(attachment.getFilePath(), "filepath");
		xmlResp.addMessage(attachment.getOriginalFileName(), "originalname");
	}

	@Override
	public XMLResponse process(String method) {

		long start_time = System.currentTimeMillis();
		try {
			if (method.equalsIgnoreCase("POST")) {
				doPOST(ses, formData, lang);
				xmlResp.status = httpStatus;
				xmlResp.type = ResponseType.JSON;
				Outcome outcome = new Outcome();
				for (Msg message : messages) {
					outcome.addMessage(message.text);
				}
				xmlResp.json = outcome;
			} else {
				doGET(ses, formData, lang);
				xmlResp.status = httpStatus;
				xmlResp.setPublishResult(toPublishElement);
				if (httpStatus == HttpStatus.SC_BAD_REQUEST) {
					xmlResp.setResponseStatus(false);
				} else {
					xmlResp.setResponseStatus(true);
				}
				for (Msg message : messages) {
					xmlResp.addMessage(message.text, message.id);
				}
				if (signal != null) {
					xmlResp.addSignal(signal);
				}
				if (xml != null) {
					xmlResp.addXMLDocumentElements(xml);
				}
				xmlResp.setRedirect(redirectURL);
			}

		} catch (Exception e) {
			xmlResp.status = HttpStatus.SC_BAD_REQUEST;
			xmlResp.setResponseStatus(false);
			xmlResp.addMessage(e.getMessage());
			println(e);
			e.printStackTrace();
		}

		xmlResp.setElapsedTime(Util.getTimeDiffInSec(start_time));
		return xmlResp;
	}

	@Override
	public XMLResponse process() {
		return process("GET");
	}

	public abstract void doGET(_Session session, _WebFormData formData, LanguageType lang) throws _Exception;

	public abstract void doPOST(_Session session, _WebFormData formData, LanguageType lang) throws _Exception;
}
