package kz.lof.exception;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import kz.flabs.servlets.SaxonTransformator;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;
import kz.lof.server.Server;
import kz.lof.webserver.servlet.IOutcomeObject;
import net.sf.saxon.s9api.SaxonApiException;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

	public ApplicationException(String appType, String error, Exception exp, LanguageCode lang) {
		super(error);
		this.appType = appType;
		StringWriter errors = new StringWriter();
		exp.printStackTrace(new PrintWriter(errors));
		exception = errors.toString();
		this.lang = lang;
	}

	@JsonIgnore
	public String getHTMLMessage() {
		return getHTMLMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@JsonIgnore
	public String getHTMLMessage(int code) {
		ExceptionXML document = new ExceptionXML(getMessage(), code, location, type, servletName, exception);
		document.setAppType(appType);
		String xslt = "webapps" + File.separator + appType + File.separator + EnvConst.ERROR_XSLT;
		File errorXslt = new File(xslt);
		if (!errorXslt.exists()) {
			errorXslt = new File("webapps" + File.separator + Environment.workspaceName + File.separator + EnvConst.ERROR_XSLT);
		}

		try {
			new SaxonTransformator().toTrans(errorXslt, document.toXML(lang));
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
		String xmlText = null;

		ExceptionXML document = new ExceptionXML(getMessage(), code, location, type, servletName, exception);
		document.setAppType(appType);
		String xslt = "webapps" + File.separator + appType + File.separator + EnvConst.ERROR_XSLT;
		File errorXslt = new File(xslt);
		if (!errorXslt.exists()) {
			errorXslt = new File("webapps" + File.separator + Environment.workspaceName + File.separator + EnvConst.ERROR_XSLT);
		}

		try {
			xmlText = new SaxonTransformator().toTrans(errorXslt, document.toXML(lang));
		} catch (IOException | SaxonApiException e) {
			Server.logger.errorLogEntry(e);
		}

		return xmlText;
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
