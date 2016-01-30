package kz.flabs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import kz.flabs.exception.PortalException;
import kz.flabs.servlets.SignalType;
import kz.flabs.servlets.pojo.Outcome;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;

public class XMLResponse {
	public ResponseType type;
	public boolean resultFlag;
	public int status;
	public Outcome json;

	protected static final String xmlTextUTF8Header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	@Deprecated
	private String responseStatus = "undefined";
	private String formSesID = "";
	private String redirectURL = "";
	private ArrayList<Script> scripts = new ArrayList<>();
	private ArrayList<Message> messages = new ArrayList<Message>();
	private HashMap<String, Message> messagesHash = new HashMap<String, Message>();
	private ArrayList<Signal> signals = new ArrayList<Signal>();
	private int messageCount = 1;
	private ArrayList<_IXMLContent> xml = new ArrayList<_IXMLContent>();
	private String elapsed_time = "";

	public XMLResponse(ResponseType type) {
		this.type = type;
	}

	public XMLResponse(ResponseType type, boolean b) {
		this.type = type;
		setResponseStatus(b);
	}

	public XMLResponse(Exception e) {
		resultFlag = false;
		responseStatus = "error";
		addMessage(e.getMessage());
		addMessage(PortalException.errorMessage(e));
	}

	public void setResponseType(ResponseType type) {
		this.type = type;
	}

	public void setRedirect(String redirectURL) {
		this.redirectURL = redirectURL.replace("&", "&amp;");
	}

	@Deprecated
	public void setResponseStatus(boolean responseStatus) {
		resultFlag = responseStatus;
		if (responseStatus) {
			this.responseStatus = "ok";
		} else {
			this.responseStatus = "error";
		}
	}

	public void setFormSesID(String fsid) {
		formSesID = "formsesid=\"" + fsid + "\"";
	}

	public void addScript(String type, String body) {
		Script script = new Script(type, body);
		scripts.add(script);
	}

	public void addMessage(String message) {
		String key = Integer.toString(messageCount);
		Message msg = new Message(message, key);
		messages.add(msg);
		messagesHash.put(key, msg);
		++messageCount;
	}

	public void setMessage(String message) {
		messages.clear();
		messagesHash.clear();
		String key = Integer.toString(messageCount);
		Message msg = new Message(message, key);
		messages.add(msg);
		messagesHash.put(key, msg);
		++messageCount;
	}

	public void addMessage(String message, String id) {
		if (id != null) {
			Message msg = new Message(message, id);
			messages.add(msg);
			messagesHash.put(id, msg);
		} else {
			addMessage(message);
		}
	}

	public void addMessage(int message, String id) {
		addMessage(Integer.toString(message), id);
	}

	public void setMessage(String message, String id) {
		messages.clear();
		messagesHash.clear();
		if (id != null) {
			Message msg = new Message(message, id);
			messages.add(msg);
			messagesHash.put(id, msg);
		} else {
			setMessage(message);
		}
	}

	public void setPublishResult(ArrayList<_IXMLContent> pulishElement) {
		this.xml = pulishElement;
	}

	public void setMessage(int message, String id) {
		setMessage(Integer.toString(message), id);
	}

	public Message getMessage(String key) {
		return messagesHash.get(key);
	}

	public void addSignal(SignalType signal) {
		signals.add(new Signal(signal));
	}

	public void addReloadSignal() {
		signals.add(new Signal(SignalType.RELOAD_PAGE));
	}

	public void addXMLDocumentElements(Collection<_IXMLContent> documents) {
		xml.addAll(documents);
	}

	public void setElapsedTime(String et) {
		elapsed_time = "elapsed_time = \"" + et + "\"";
	}

	public String toXML() {
		StringBuffer result = new StringBuffer(100);
		result.append("<response type=\"" + type + "\" status=\"" + responseStatus + "\" " + elapsed_time + ">");
		for (Signal sig : signals) {
			result.append(sig.toXML());
		}
		for (Message msg : messages) {
			result.append(msg.toXML());
		}
		for (Script script : scripts) {
			result = new StringBuffer(100);
			result.append(script.toXML());
			return result.toString();
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

		result.append("<redirect>" + redirectURL + "</redirect>");
		result.append("</response>");
		return result.toString();
	}

	public String toCompleteXML() {
		return xmlTextUTF8Header + toXML();
	}

	public class Message {
		public String text;
		String id;

		Message(String text, String id) {
			this.text = text;
			this.id = id;
		}

		String toXML() {
			return "<message id=\"" + id + "\" " + formSesID + ">" + XMLUtil.getAsTagValue(text) + "</message>";
		}

	}

	@Deprecated
	class Signal {
		SignalType signal;

		Signal(SignalType signal) {
			this.signal = signal;
		}

		String toXML() {
			return "<signal>" + signal.toString() + "</signal>";
		}

	}

	class Script {
		String type;
		String body;

		Script(String type, String body) {
			this.type = type;
			this.body = body;
		}

		String toXML() {
			return "<script type=\"" + type + "\">" + body + "</script>";
		}

	}
}
