package kz.lof.webserver.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.flabs.localization.LanguageCode;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.SaxonTransformator;
import kz.flabs.servlets.pojo.OutcomeType;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.nextbase.script._Validation;
import net.sf.saxon.s9api.SaxonApiException;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PageOutcome {
	public PublishAsType publishAs = PublishAsType.HTML;
	public String name;
	public boolean disableClientCache;
	private int httpStatus = HttpStatus.SC_OK;
	private static final String xmlTextUTF8Header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	private List<PageOutcome> includedPage = new ArrayList<PageOutcome>();
	private ArrayList<IOutcomeObject> objects = new ArrayList<IOutcomeObject>();
	private _Session ses;
	private LanguageCode lang;
	private OutcomeType type = OutcomeType.OK;
	private Map<String, String> captions = new HashMap<String, String>();
	private boolean isScriptResult;
	private String pageId;
	private String redirectURL;
	private String flash;
	private String filePath, fileName;
	private _Validation validation;

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

	public void setObject(IOutcomeObject obj) {
		objects.clear();
		objects.add(obj);
	}

	public void addObject(IOutcomeObject obj) {
		objects.add(obj);
	}

	public void setValidation(_Validation obj) {
		validation = obj;
	}

	public void setBadRequest() {
		httpStatus = HttpStatus.SC_BAD_REQUEST;
	}

	public void setVeryBadRequest() {
		httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
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

	public void setFile(String fp, String fn) {
		filePath = fp;
		fileName = fn;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getFileName() {
		return fileName;
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

		if (validation != null) {
			result.append(validation.toXML());
		}

		for (IOutcomeObject xmlContent : objects) {
			result.append(xmlContent.toXML());
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
		clazz.setCaptions(captions);
		clazz.setType(type);
		clazz.setRedirectURL(redirectURL);
		clazz.setFlash(flash);
		clazz.setValidation(validation);

		ObjectMapper mapper = new ObjectMapper();
		// mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		// SimpleModule customSerializerModule = new SimpleModule();
		// customSerializerModule.addSerializer(_POJOListWrapper.class, new
		// POJOObjectSerializer());
		// mapper.registerModule(customSerializerModule);
		String jsonInString = null;
		try {
			jsonInString = mapper.writeValueAsString(clazz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonInString;
	}

	public void setContent(IOutcomeObject wrappedObj) {
		addContent(wrappedObj);

	}

	public void addContent(IOutcomeObject element) {
		objects.add(element);

	}

	public void addContent(List<IOutcomeObject> elements) {
		objects.addAll(elements);

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

	// TODO Probably it is object is not needed because it can be serialize
	// straight through PageOutcome instance
	@JsonRootName("outcome")
	class JSONClass {
		private ArrayList<IOutcomeObject> objects = new ArrayList<IOutcomeObject>();
		private Map<String, String> captions;
		private OutcomeType type;
		private String redirectURL;
		private String flash;
		private _Validation validation;

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

		@JsonIgnore
		public ArrayList<IOutcomeObject> getObjects() {
			return objects;
		}

		public void setObjects(ArrayList<IOutcomeObject> objects) {
			this.objects = objects;
		}

		public void setValidation(_Validation vp) {
			this.validation = vp;
		}

		public _Validation getValidation() {
			return this.validation;
		}
	}
}
