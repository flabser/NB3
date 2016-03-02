package kz.lof.exception;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import kz.flabs.servlets.SaxonTransformator;
import kz.flabs.servlets.pojo.OutcomeType;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;
import kz.lof.server.Server;
import kz.lof.webserver.servlet.IOutcomeObject;
import kz.lof.webserver.servlet.JSONClass;
import net.sf.saxon.s9api.SaxonApiException;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ApplicationException extends Exception implements IOutcomeObject {
	private static final long serialVersionUID = 1L;
	private String location;
	private String type = "APPLICATION";
	private String servletName = "";
	private String exception;
	private String appType;
	private LanguageCode lang;
	private int code;

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

	public ApplicationException(Response r) {

	}

	public String getHTMLMessage() {
		return getHTMLMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public String getHTMLMessage(int code) {
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
		JSONClass clazz = new JSONClass();
		List<IOutcomeObject> list = new ArrayList<IOutcomeObject>();
		list.add(this);
		clazz.setObjects(list);
		clazz.setType(OutcomeType.SERVER_ERROR);

		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		;
		String jsonInString = null;
		try {
			jsonInString = mapper.writeValueAsString(clazz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonInString;
	}
}
