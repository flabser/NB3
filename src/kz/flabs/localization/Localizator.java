package kz.flabs.localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.lof.env.Environment;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Localizator implements Const {

	public Vocabulary populate() {
		return fill(Environment.vocabuarFilePath, "system", new HashMap<String, Sentence>());
	}

	public Vocabulary populate(String appName, String vocabuarFilePath) {
		return fill(vocabuarFilePath, appName, Environment.vocabulary.words);
	}

	private Vocabulary fill(String vocabuarFilePath, String appName, HashMap<String, Sentence> w) {
		try {
			File docFile = new File(vocabuarFilePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document vocDoc = db.parse(docFile.toString());
			return new Vocabulary(vocDoc, appName, w);
		} catch (FileNotFoundException e) {
			AppEnv.logger.warningLogEntry("Vocabulary file not found (path=\"" + vocabuarFilePath + "\")");
		} catch (ParserConfigurationException e) {
			AppEnv.logger.errorLogEntry(e);
		} catch (IOException e) {
			AppEnv.logger.errorLogEntry(e);
		} catch (SAXException e) {
			AppEnv.logger.errorLogEntry(e);
		}
		return null;
	}
}
