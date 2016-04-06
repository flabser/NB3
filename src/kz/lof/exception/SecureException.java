package kz.lof.exception;

import kz.lof.localization.LanguageCode;
import kz.lof.scriptprocessor.page.IOutcomeObject;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SecureException extends ApplicationException implements IOutcomeObject {
	private static final long serialVersionUID = 1L;

	public SecureException(String appType, String error, LanguageCode lang) {
		super(error, error, lang);
		this.appType = appType;
		this.lang = lang;
		code = HttpStatus.SC_FORBIDDEN;
	}

	@Override
	@JsonIgnore
	public String getHTMLMessage() {
		return getHTMLMessage(HttpStatus.SC_FORBIDDEN);
	}

	@Override
	public Object toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixIn(SecureException.class, MapperMixIn.class);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		String jsonInString = null;
		try {
			jsonInString = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonInString;
	}
}
