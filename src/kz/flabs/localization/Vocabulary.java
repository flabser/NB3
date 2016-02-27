package kz.flabs.localization;

import java.util.HashMap;

import kz.flabs.util.XMLUtil;
import kz.pchelka.log.Log4jLogger;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Vocabulary {
	public HashMap<String, Sentence> words;
	public String appName;
	private static Log4jLogger logger = new Log4jLogger("Vocabulary");

	public Vocabulary(String appName) {
		this.appName = appName;
		this.words = new HashMap<String, Sentence>();
	}

	public Vocabulary(Document doc, String appName, HashMap<String, Sentence> w) {
		this.appName = appName;
		this.words = (HashMap<String, Sentence>) w.clone();
		org.w3c.dom.Element root = doc.getDocumentElement();
		LanguageCode primaryLang = LanguageCode.valueOf(XMLUtil.getTextContent(doc, "/vocabulary/@primary", true, "UNKNOWN", false).toUpperCase());

		NodeList nodename = root.getElementsByTagName("sentence");
		for (int i = 0; i < nodename.getLength(); i++) {
			Sentence sentence = new Sentence(nodename.item(i), primaryLang.toString());
			if (sentence.isOn) {
				words.put(sentence.keyWord, sentence);
			}
		}
	}

	@Deprecated
	public String[] getWord(String keyWord, String lang) {
		String returnVal[] = new String[2];
		Sentence sent = words.get(keyWord);
		if (sent == null && keyWord != "№") {

			logger.warningLogEntry("Translation of word \"" + keyWord + "\" to " + lang + ", has not found in vocabulary (" + appName + ")");

			returnVal[0] = keyWord;
			returnVal[1] = "";
			return returnVal;
		} else {
			SentenceCaption caption = sent.words.get(lang);
			returnVal[0] = caption.word;
			returnVal[1] = caption.hint;
			return returnVal;
		}
	}

	public String getWord(String keyWord, LanguageCode lang) {
		try {
			Sentence sent = words.get(keyWord);
			if (sent == null) {
				logger.warningLogEntry("Translation of word \"" + keyWord + "\" to " + lang + ", has not found in vocabulary (" + appName + ")");
				return keyWord;
			} else {
				SentenceCaption caption = sent.words.get(lang.name());
				return caption.word;
			}
		} catch (Exception e) {
			return keyWord;
		}
	}

	public SentenceCaption getSentenceCaption(String keyWord, String lang) {
		Sentence sent = words.get(keyWord.trim());
		if (sent == null) {

			logger.warningLogEntry("Translation of word \"" + keyWord + "\" to " + lang + ", has not found in vocabulary (" + appName + ")");
			;
			SentenceCaption primary = new SentenceCaption(keyWord, keyWord);
			return primary;
		} else {
			SentenceCaption caption = sent.words.get(lang);
			if (caption != null) {
				return caption;
			} else {
				SentenceCaption primary = new SentenceCaption(keyWord, keyWord);
				return primary;
			}

		}
	}

	public StringBuffer toXML(String lang) {
		StringBuffer output = new StringBuffer(1000);

		return output;
	}

}
