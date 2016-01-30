package kz.flabs.runtimeobj.forum;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.ISaveField;

import java.util.Date;
import java.util.HashMap;

public class Topic extends Document{
	public int docType = Const.DOCTYPE_TOPIC;
	public String sourceURL;

	
	private static final long serialVersionUID = 1L;

	public Topic(AppEnv env, User currentUser) {
		super(env, currentUser);	
		docType = Const.DOCTYPE_TOPIC;
	}

	public Topic(IDatabase db, String currentUser) {
		super(db, currentUser);
		docType = Const.DOCTYPE_TOPIC;
	}

	public void setShared(int value) {
        addNumberField("shared", value);
	}
	
	public int isShared() throws DocumentException {
		return getValueAsInteger("shared");
	}
	
	public String getTheme() throws DocumentException {
		return getValueAsString("theme")[0];
	}
	
	public void setViewText() throws DocumentException {
		if (this.getContent() != null ) {
			String viewtext = Util.removeHTMLTags(this.getContent());
			this.setViewText(viewtext);
		}
	}
	
	public void setTheme(String value) {
		addStringField("theme", value);
	}
	
	public int getStatus() throws DocumentException {
		return getValueAsInteger("status");
	}
	
	public void setStatus(int value) {
        addNumberField("status", value);
	}
	
	public int getCitationIndex() throws DocumentException {
		return getValueAsInteger("citationindex");
	}
	
	public void setCitationIndex(int value) {
        addNumberField("citationindex", value);
	}
	
	public String getContent() {
        try {
            return getValueAsString("contentsource")[0];
        } catch (Exception e) {
            AppEnv.logger.errorLogEntry(e);
        }
        return "";
	}
	
	public void setContent(String value) {
		addStringField("contentsource", value);
	}
	
	public void setTopicDate(Date value) {
		addDateField("topicdate", value);
	}
	
	public Date getTopicDate() throws DocumentException{
		return getValueAsDate("topicdate");
	}
	
	public String getURL() {	
		return "Provider?type=edit&element=document&id=topic&key=" + docID;
	}

	public int save(User user) throws DocumentAccessException, DocumentException{
		int docID = 0;
		normalizeViewTexts();
		if(isNewDoc()){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = db.getForum().insertTopic(this, user);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			docID = db.getForum().updateTopic(this, user);
		}
		return docID;
	}
	
	
	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		
//		setDefaultRuleID(getValueForDoc(saveFieldsMap, "defaultruleid", fields).valueAsText);
		setForm(getValueForDoc(saveFieldsMap, "form", fields).valueAsText);
		setCitationIndex(getValueForDoc(saveFieldsMap, "citationindex", fields).valueAsNumber.intValue());
		setContent(getValueForDoc(saveFieldsMap, "contentsource", fields).valueAsText);
		setShared(getValueForDoc(saveFieldsMap, "shared", fields).valueAsNumber.intValue());
		setStatus(getValueForDoc(saveFieldsMap, "status", fields).valueAsNumber.intValue());
		setTheme(getValueForDoc(saveFieldsMap, "theme", fields).valueAsText);
		setTopicDate(new Date());
		
		for(ISaveField saveField: saveFieldsMap.values()){
			if (saveField.getSourceType() == ValueSourceType.WEBFORMFILE){
				blobDataProcess(saveField, fields);	
			}
		}

	}
}
