package kz.lof.webserver.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.flabs.localization.LanguageType;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.SaxonTransformator;
import kz.flabs.servlets.pojo.OutcomeType;
import kz.nextbase.script._IPOJOObject;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.pchelka.env.Environment;
import net.sf.saxon.s9api.SaxonApiException;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

public class PageOutcome {
	public PublishAsType publishAs = PublishAsType.HTML;
	public String name;
	public boolean disableClientCache;
	private int httpStatus = HttpStatus.SC_OK;
	private static final String xmlTextUTF8Header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	private List<PageOutcome> includedPage = new ArrayList<PageOutcome>();
	private ArrayList<String> messages = new ArrayList<String>();
	private ArrayList<_IPOJOObject> objects = new ArrayList<_IPOJOObject>();
	private _Session ses;
	private LanguageType lang;
	private OutcomeType type;
	private Map<String, String> captions = new HashMap<String, String>();
	private boolean isScriptResult;
	private String pageId;
	private String redirectURL;
	private String flash;

	public void setSession(_Session ses) {
		this.ses = ses;
		lang = ses.getLang();
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

	public void addMessage(String message) {
		messages.add(message);
	}

	public void setObject(_IPOJOObject obj) {
		objects.clear();
		objects.add(obj);
	}

	public void addObject(_IPOJOObject obj) {
		objects.add(obj);
	}

	public void setError(Exception e) {
		setMessage(e.toString());
		objects.clear();
		httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;

	}

	public void setBadRequest() {
		httpStatus = HttpStatus.SC_BAD_REQUEST;
	}

	public void setType(OutcomeType type) {
		this.type = type;
		String keyWord = "";
		if (type == OutcomeType.OK) {
			keyWord = "action_completed_successfully";
		} else if (type == OutcomeType.DOCUMENT_SAVED) {
			keyWord = "document_was_saved_succesfully";
		} else if (type == OutcomeType.SERVER_ERROR) {
			keyWord = "internal_server_error";
		} else if (type == OutcomeType.VALIDATION_ERROR) {
			keyWord = "validation_error";
		}
		captions.put("type", Environment.vocabulary.getWord(keyWord, lang));
	}

	public void setScriptResult(boolean isScriptResult) {
		this.isScriptResult = isScriptResult;
	}

	public String getValue() throws IOException, SaxonApiException {
		if (publishAs == PublishAsType.HTML) {
			SaxonTransformator st = new SaxonTransformator();
			return st.toTrans(null, toCompleteXML());
		} else if (publishAs == PublishAsType.JSON) {
			return getJSON();
		} else {
			return toCompleteXML();
		}

	}

	public String getFlash() {
		return flash;
	}

	public void setFlash(String flash) {
		this.flash = flash;
	}

	public String toXML() {
		StringBuffer result = new StringBuffer(100);
		result.append("<page>");
		if (name != null) {
			result.append("<" + name + ">");
		}
		if (isScriptResult) {
			result.append("<response type=\"RESULT_OF_PAGE_SCRIPT\"><content>");
		}
		for (String msg : messages) {
			result.append(msg);
		}

		for (_IPOJOObject xmlContent : objects) {
			result.append(xmlContent.getFullXMLChunk(lang));
		}

		for (PageOutcome included : includedPage) {
			result.append(included.toXML());
		}

		if (isScriptResult) {
			result.append("</content></response>");
		}
		if (name != null) {
			result.append("</" + name + ">");
		}

		StringBuffer captionsText = new StringBuffer(100);
		for (String capKey : captions.keySet()) {
			String translatedVal = captions.get(capKey);
			captionsText.append("<" + capKey + " caption=\"" + translatedVal + "\"></" + capKey + ">");
		}

		result.append("<captions>" + captionsText.toString() + "</captions></page>");
		return result.toString();
	}

	public String toCompleteXML() {
		String localUserName = ses.getUser().getUserName();
		String userId = ses.getUser().getUserID();
		String lang = ses.getLang().name();

		return xmlTextUTF8Header + "<request  lang=\"" + lang + "\" id=\"" + pageId + "\" userid=\"" + userId + "\" username=\"" + localUserName
		        + "\">" + toXML() + "</request>";

	}

	public String getJSON() {
		JSONClass clazz = new JSONClass();
		clazz.setObjects(objects);
		clazz.setMessages(messages);
		clazz.setCaptions(captions);
		clazz.setType(type);
		clazz.setRedirectURL(redirectURL);
		clazz.setFlash(flash);
		// Gson gson = new
		// GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		// gson = new GsonBuilder().setExclusionStrategies(new ExclStrat())
		// .serializeNulls() <-- uncomment to serialize NULL fields as well
		// .create();
		Gson gson = new Gson();
		return gson.toJson(clazz);
	}

	public void setContent(_POJOObjectWrapper wrappedObj) {
		addContent(wrappedObj);

	}

	public void addContent(_IPOJOObject element) {
		objects.add(element);

	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setCaptions(HashMap<String, String> captions) {
		this.captions.putAll(captions);
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	class JSONClass {
		@Expose
		private ArrayList<_IPOJOObject> objects = new ArrayList<_IPOJOObject>();
		@Expose
		private ArrayList<String> messages;
		@Expose
		private Map<String, String> captions;
		@Expose
		private OutcomeType type;
		@Expose
		private String redirectURL;
		@Expose
		private String flash;

		public ArrayList<String> getMessages() {
			return messages;
		}

		public void setMessages(ArrayList<String> messages) {
			this.messages = messages;
		}

		public Map<String, String> getCaptions() {
			return captions;
		}

		public void setCaptions(Map<String, String> captions) {
			this.captions = captions;
		}

		public OutcomeType getType() {
			return type;
		}

		public void setType(OutcomeType type) {
			this.type = type;
		}

		public String getRedirectURL() {
			return redirectURL;
		}

		public void setRedirectURL(String redirectURL) {
			this.redirectURL = redirectURL;
		}

		public String getFlash() {
			return flash;
		}

		public void setFlash(String flash) {
			this.flash = flash;
		}

		public ArrayList<_IPOJOObject> getObjects() {
			return objects;
		}

		public void setObjects(ArrayList<_IPOJOObject> objects) {
			this.objects = objects;
		}

	}
}
