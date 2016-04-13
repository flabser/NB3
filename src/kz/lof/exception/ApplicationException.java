package kz.lof.exception;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import kz.flabs.servlets.SaxonTransformator;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;
import kz.lof.scriptprocessor.page.IOutcomeObject;
import kz.lof.scriptprocessor.page.PageOutcome;
import kz.lof.server.Server;
import net.sf.saxon.s9api.SaxonApiException;

public class ApplicationException extends Exception implements IOutcomeObject {
	private static final long serialVersionUID = 1L;
	private String location;
	private String type = EnvConst.APP_NAME;
	private String servletName = "Provider";
	private String exception;
	private String appType;
	private LanguageCode lang;
	private int code = HttpStatus.SC_INTERNAL_SERVER_ERROR;

	public ApplicationException(String appType, String error, LanguageCode lang) {
		super(error);
		this.appType = appType;
		this.lang = lang;
	}

	public ApplicationException(String appType, PageOutcome outcome, LanguageCode lang) {
		super(outcome.getException().toString());
		this.appType = appType;
		StringWriter errors = new StringWriter();
		outcome.getException().printStackTrace(new PrintWriter(errors));
		exception = errors.toString();
		this.lang = lang;
		code = outcome.getHttpStatus();
	}

	@JsonIgnore
	public String getHTMLMessage() {
		return getHTMLMessage(code);
	}

	@JsonIgnore
	public String getHTMLMessage(int code) {
		ExceptionXML document = new ExceptionXML(getMessage(), code, location, type, servletName, exception);
		document.setAppType(appType);
		String xslt = Environment.getKernelDir() + "xsl" + File.separator + EnvConst.ERROR_XSLT;
		File errorXslt = new File(xslt);

		try {
			String xml = document.toXML(lang);
			return new SaxonTransformator().toTrans(errorXslt, xml);
		} catch (IOException | SaxonApiException e) {
			Server.logger.errorLogEntry(e);
		}

		return toXML();
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toXML() {
		ExceptionXML document = new ExceptionXML(getMessage(), code, location, type, servletName, exception);
		document.setAppType(appType);
		return document.toXML(lang);
	}

	@Override
	public Object toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixIn(ApplicationException.class, MapperMixIn.class);
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
