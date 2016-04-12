package kz.lof.scriptprocessor.scheduled;

import java.util.ArrayList;
import java.util.List;

import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.scriptprocessor.page.IOutcomeObject;
import kz.lof.scriptprocessor.page.JSONClass;
import kz.lof.scriptprocessor.page.OutcomeType;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ScheduledTaskOutcome {
	public String name;
	private List<ScheduledTaskOutcome> includedPage = new ArrayList<ScheduledTaskOutcome>();
	private ArrayList<IOutcomeObject> objects = new ArrayList<IOutcomeObject>();
	private _Session ses;
	private LanguageCode lang;
	private OutcomeType type = OutcomeType.OK;
	private boolean isScriptResult;
	private Exception exception;

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

	public void addPageOutcome(ScheduledTaskOutcome o) {
		includedPage.add(o);
	}

	public void setObject(IOutcomeObject obj) {
		objects.clear();
		objects.add(obj);
	}

	public void addObject(IOutcomeObject obj) {
		objects.add(obj);
	}

	public OutcomeType getType() {
		return type;
	}

	public void setScriptResult(boolean isScriptResult) {
		this.isScriptResult = isScriptResult;
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

		for (IOutcomeObject xmlContent : objects) {
			result.append(xmlContent.toXML());
		}

		for (ScheduledTaskOutcome included : includedPage) {
			result.append(included.toXML());
		}

		if (isScriptResult) {
			result.append("</content></response>");
		}
		if (name != null) {
			result.append("</" + name + ">");
		}

		return result.toString();
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getJSON() {
		JSONClass clazz = new JSONClass();

		clazz.setObjects(objects);

		clazz.setType(type);

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
		// System.out.println(jsonInString);
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

}
