package kz.lof.webserver.servlet;

import java.util.ArrayList;
import java.util.Map;

import kz.flabs.servlets.pojo.OutcomeType;
import kz.lof.scripting._Validation;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("outcome")
public class JSONClass {
	private ArrayList<Object> objects = new ArrayList<Object>();
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

	public ArrayList<Object> getObjects() {
		return objects;
	}

	public void setObjects(ArrayList<IOutcomeObject> objects) {
		for (IOutcomeObject obj : objects) {
			this.objects.add(obj.toJSON());
		}
	}

	public void setValidation(_Validation vp) {
		this.validation = vp;
	}

	public _Validation getValidation() {
		return this.validation;
	}
}