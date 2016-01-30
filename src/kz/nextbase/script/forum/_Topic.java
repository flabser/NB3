package kz.nextbase.script.forum;

import java.util.Date;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.forum.Topic;
import kz.nextbase.script._Document;
import kz.nextbase.script._Session;

public class _Topic  extends _Document{

	protected String currentUserID;
	private Topic topic;
	private _Session session;

	public _Topic(Topic topic, _Session ses){
		super(topic, ses);	
		session = ses;
		currentUserID = session.getCurrentUserID();
		this.topic = topic;
	}

	public String getTheme() throws DocumentException {
		return topic.getTheme();
	}

	public void setTheme(String value) {
		topic.setTheme(value);
	}

	
	
	public int getStatus() throws DocumentException {
		return topic.getStatus();
	}

	public void setStatus(int value) {
		topic.setStatus(value);
	}

	public void setCitationIndex(int val){
		topic.setCitationIndex(val);
	}
	
	public int getCitationIndex() throws DocumentException {
		return 0;

	}

	

	public String getContent() throws DocumentException {
		return null;

	}

	public void setShared(int value) {
		topic.setShared(value);
	}
	
	public void setContent(String value) {
		topic.setContent(value);
	}

	public void setTopicDate(Date value) {
		topic.setTopicDate(value);
	}

	public Date getTopicDate() throws DocumentException{
		return null;

	}

	public String getURL() {	
		return topic.getURL();
	}

}
