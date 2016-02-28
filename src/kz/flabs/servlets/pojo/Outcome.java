package kz.flabs.servlets.pojo;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.runtimeobj.xml.XMLDocument;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Tag;

public class Outcome implements _IXMLContent {
	private OutcomeType type = OutcomeType.OK;
	private List<String> message = new ArrayList<String>();

	private XMLDocument document;

	public Outcome() {

	}

	@Deprecated
	public Outcome(_Tag rootTag) {
		document = new XMLDocument(rootTag.getRuntimeTag());
	}

	public OutcomeType getType() {
		return type;
	}

	public Outcome setType(OutcomeType type) {
		this.type = type;
		return this;
	}

	public Outcome addMessage(String message) {
		this.message.add(message);
		return this;
	}

	public Outcome setErrorMessage(Exception e) {
		return setMessage(e.getLocalizedMessage());
	}

	public Outcome setMessage(String s) {
		message.clear();
		addMessage(s);
		return this;
	}

	@Override
	public String toXML() throws _Exception {
		return document.toXML();
	}

	@Override
	public Object toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
