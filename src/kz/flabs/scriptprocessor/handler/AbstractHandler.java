package kz.flabs.scriptprocessor.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import kz.flabs.localization.Vocabulary;
import kz.flabs.scriptprocessor.Msg;
import kz.flabs.scriptprocessor.ScriptEvent;
import kz.flabs.scriptprocessor.ScriptProcessorUtil;
import kz.flabs.servlets.SignalType;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.flabs.util.PageResponse;
import kz.lof.scripting._Session;
import kz.nextbase.script._WebFormData;

public abstract class AbstractHandler extends ScriptEvent implements IHandlerScript {
	public String lang;
	public ArrayList<Msg> messages = new ArrayList<Msg>();

	private _Session ses;
	private Map<String, String[]> formData;
	private Vocabulary vocabulary;
	private SignalType signal;
	private File file;
	private PageResponse xmlResp = new PageResponse(ResponseType.RESULT_OF_HANDLER_SCRIPT);
	private _WebFormData webFormData;

	@Override
	public void setSession(_Session ses) {
		this.ses = ses;
	}

	@Override
	@Deprecated
	public void setFormData(Map<String, String[]> formData) {
		this.formData = formData;

	}

	@Override
	public void setWebFormData(_WebFormData formData) {
		webFormData = formData;

	}

	@Override
	public void setCurrentLang(String lang, Vocabulary vocabulary) {
		this.lang = lang;
		this.vocabulary = vocabulary;
	}

	public void msgBox(String m) {
		messages.add(new Msg(null, m));
	}

	public void localizedMsgBox(String m) {
		String msg = vocabulary.getWord(m, lang)[0];
		messages.add(new Msg(null, msg));
	}

	public void msgBox(String m, String id) {
		messages.add(new Msg(id, m));
	}

	public void localizedMsgBox(String m, String id) {
		String msg = vocabulary.getWord(m, lang)[0];
		messages.add(new Msg(id, msg));
	}

	public void sendSignalToRefresh() {
		signal = SignalType.RELOAD_PAGE;
	}

	public void showFile(File file) {
		this.file = file;
		xmlResp = new PageResponse(ResponseType.SHOW_FILE_AFTER_HANDLER_FINISHED);
		xmlResp.addMessage(file.getAbsolutePath(), "full_file_path");
		xmlResp.addMessage(file.getName(), "original_name");
	}

	@Override
	@Deprecated
	public PageResponse process() {
		long start_time = System.currentTimeMillis();
		try {
			doHandler(ses, formData, lang);
			xmlResp.setResponseStatus(true);
			for (Msg message : messages) {
				xmlResp.addMessage(message.text, message.id);
			}
		} catch (Exception e) {
			xmlResp.setResponseStatus(false);
			xmlResp.setMessage(e.toString());
			xmlResp.addMessage(ScriptProcessorUtil.getGroovyError(e.getStackTrace()));
			e.printStackTrace();
		}
		xmlResp.setElapsedTime(Util.getTimeDiffInSec(start_time));
		return xmlResp;
	}

	@Override
	public PageResponse run() {
		long start_time = System.currentTimeMillis();

		try {
			doHandler(ses, webFormData);
			xmlResp.setResponseStatus(true);
			for (Msg message : messages) {
				xmlResp.addMessage(message.text, message.id);
			}

		} catch (Exception e) {
			xmlResp.setResponseStatus(false);
			xmlResp.setMessage(e.toString());
			xmlResp.addMessage(ScriptProcessorUtil.getGroovyError(e.getStackTrace()));
			e.printStackTrace();
		}
		xmlResp.setElapsedTime(Util.getTimeDiffInSec(start_time));
		return xmlResp;
	}

	@Deprecated
	public abstract void doHandler(_Session session, Map<String, String[]> formData, String lang);

	public abstract void doHandler(_Session session, _WebFormData formData);

}
