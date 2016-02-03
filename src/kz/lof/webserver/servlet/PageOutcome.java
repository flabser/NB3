package kz.lof.webserver.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kz.flabs.localization.LanguageType;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.SaxonTransformator;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import net.sf.saxon.s9api.SaxonApiException;

public class PageOutcome {
	public PublishAsType publishAs = PublishAsType.HTML;
	public String name;
	public boolean disableClientCache;

	protected static final String xmlTextUTF8Header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	private List<PageOutcome> includedPage = new ArrayList<PageOutcome>();
	private ArrayList<String> messages = new ArrayList<String>();
	private ArrayList<_IXMLContent> xml = new ArrayList<_IXMLContent>();
	private _Session ses;

	PageOutcome(_Session ses) {
		this.ses = ses;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addPageOutcome(PageOutcome o) {
		includedPage.add(o);
	}

	public void setMessage(String message) {
		messages.clear();
		messages.add(message);
	}

	public void addMessage(String message, String id) {
		messages.add(message);
	}

	public void addMessage(int message, String id) {
		addMessage(Integer.toString(message), id);
	}

	public void setPublishResult(ArrayList<_IXMLContent> pulishElement) {
		this.xml = pulishElement;
	}

	public void addXMLDocumentElements(Collection<_IXMLContent> documents) {
		xml.addAll(documents);
	}

	public String getValue() throws IOException, SaxonApiException {
		if (publishAs == PublishAsType.HTML) {
			SaxonTransformator st = new SaxonTransformator();
			return st.toTrans(null, toCompleteXML(ses));
		} else if (publishAs == PublishAsType.JSON) {
			return toJSON(ses);
		} else {
			return toCompleteXML(ses);
		}
	}

	private String toJSON(_Session ses2) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toXML() {
		StringBuffer result = new StringBuffer(100);
		result.append("<response>");

		for (String msg : messages) {
			// result.append(msg.toXML());
		}

		if (xml != null) {
			try {
				result.append("<content>");

				for (_IXMLContent xmlContent : xml) {
					result.append(xmlContent.toXML());
				}
				result.append("</content>");
			} catch (_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		result.append("</response>");
		return result.toString();
	}

	public String toCompleteXML(_Session ses) {
		String localUserName = ses.getCurrentUserID();
		LanguageType lang = ses.getLang();
		String id = null;
		// localUserName = userSession.currentUser.getUserName();

		/*
		 * String queryString = request.getQueryString(); if (queryString !=
		 * null) { queryString = "querystring=\"" + queryString.replace("&",
		 * "&amp;") + "\""; } else { queryString = ""; }
		 */

		return xmlTextUTF8Header + "<request  lang=\"" + lang + "\" id=\"" + id + "\" userid=\"" + localUserName + "\" username=\"" + localUserName
		        + "\">" + toXML() + "</request>";

	}
}
