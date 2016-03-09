package kz.flabs.scriptprocessor;

import java.sql.Connection;
import java.util.Map;

import kz.lof.scripting._Session;

public class ScriptSource implements IScriptSource {
	private _Session session;
	private String lang;

	private Map<String, String[]> formData;
	private Connection connection;

	@Override
	public void setSession(_Session ses) {
		session = ses;
	}

	@Override
	public void setLang(String lang) {
		this.lang = lang;
	}

	@Override
	public void setFormData(Map<String, String[]> formData) {
		this.formData = formData;
	}

	@Override
	public void setConnection(Connection conn) {
		connection = conn;
	}

	@Override
	public String providerHandlerProcess() throws Exception {
		return doHandler(session, formData);
	}

	public String doHandler(_Session session, Map<String, String[]> formData) {
		return "";
	}

	@Override
	public String patchHandlerProcess() throws Exception {
		return doHandler(session, connection);
	}

	public String doHandler(_Session session, Connection conn) {
		return "";
	}

	@Override
	public void setUser(String user) {
	}

	@Override
	public String[] simpleProcess() {
		return getStringValue();
	}

	public String[] getStringValue() {
		return getBlankValue();
	}

	@Override
	public String[] sessionProcess() {
		try {
			return getStringValue(session);
		} catch (Exception e) {
			String[] result = { "" };
			return result;
		}
	}

	@Override
	public String[] sessionLangProcess() {
		try {
			return getStringValue(session, lang);
		} catch (Exception e) {
			String[] result = { "" };
			return result;
		}
	}

	public String[] getStringValue(_Session session, String lang) {
		return getBlankValue();
	}

	public String[] getStringValue(_Session session) {
		return getBlankValue();
	}

	public static String[] getBlankValue() {
		String[] result = { "" };
		return result;
	}

	@Override
	public String getConsoleOutput() {
		return null;
	}

	/* script helper */
	public String[] getAsArray(String val) {
		String[] result = { val };
		return result;
	}

	@Override
	public String[] documentProcess() {

		return null;
	}

	@Override
	public String[] documentLangProcess() {

		return null;
	}

	@Override
	public String schedulerHandlerProcess() throws Exception {
		return null;
	}

}
