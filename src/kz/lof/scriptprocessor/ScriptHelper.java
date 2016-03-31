package kz.lof.scriptprocessor;

import java.util.ArrayList;

import kz.flabs.localization.Vocabulary;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.nextbase.script._IXMLContent;

public class ScriptHelper {
	protected Vocabulary vocabulary;
	protected String redirectURL = "";
	protected ArrayList<_IXMLContent> toPublishElement = new ArrayList<_IXMLContent>();
	private _Session session;

	public String getTmpDirPath() {
		return Environment.tmpDir;
	}

	public _Session getSes() {
		return session;
	}

	public void setSes(_Session ses) {
		this.session = ses;
		vocabulary = ses.getAppEnv().vocabulary;
	}

	public String getWord(String word, Vocabulary vocabulary, String lang) {
		try {
			return vocabulary.getSentenceCaption(word, lang).word;
		} catch (Exception e) {
			return word.toString();
		}
	}

	public String getLocalizedWord(String word, LanguageCode lang) {
		return getWord(word, vocabulary, lang.name());
	}

	public void devPrint(Object text) {
		if (Environment.isDevMode()) {
			System.out.println(text.toString());
		}
	}

	public void println(Object text) {
		System.out.println(text.toString());
	}

	public void log(String text) {
		Server.logger.infoLogEntry(text);
	}

	public static void error(Exception e) {
		ScriptProcessor.logger.errorLogEntry(e);
	}

}
