package kz.flabs.localization;

import java.util.HashMap;

import kz.flabs.util.XMLUtil;
import kz.lof.localization.LanguageCode;
import kz.lof.server.Server;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Sentence {
	public boolean isOn = true;
	public boolean isValid;
	public LanguageCode lang;
	public String keyWord;
	public int code;
	public HashMap<String, SentenceCaption> words = new HashMap<String, SentenceCaption>();

	Sentence() {

	}

	Sentence(Node node, String primaryLang) {
		try {
			if (!XMLUtil.getTextContent(node, "@mode", false).equals("on")) {
				isOn = false;
				return;
			}
			keyWord = XMLUtil.getTextContent(node, "@keyword", false);
			code = XMLUtil.getNumberContent(node, "@code", 0);
			String primaryHint = XMLUtil.getTextContent(node, "@hint", false);
			SentenceCaption primary = new SentenceCaption(keyWord, primaryHint);
			words.put(primaryLang, primary);
			NodeList entries = XMLUtil.getNodeList(node, "entry");
			for (int i = 0; i < entries.getLength(); i++) {
				Node wordNode = entries.item(i);
				LanguageCode l = LanguageCode.valueOf(XMLUtil.getTextContent(wordNode, "@lang", true, "UNKNOWN", false).toUpperCase());
				String hint = XMLUtil.getTextContent(wordNode, "@hint", false);
				SentenceCaption c = new SentenceCaption(wordNode.getTextContent(), hint);
				words.put(l.toString(), c);
			}

		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

	@Override
	public String toString() {
		return "keyword=" + keyWord + ", words=" + words;
	}

}
