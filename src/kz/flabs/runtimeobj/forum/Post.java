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

public class Post extends Document {
	public int docType = Const.DOCTYPE_POST;
	public String sourceURL;

	
	private static final long serialVersionUID = 1L;

	public Post(AppEnv env, User currentUser) {
		super(env, currentUser);	
		docType = Const.DOCTYPE_POST;
	}

	public Post(IDatabase db, String currentUser) {
		super(db, currentUser);
		docType = Const.DOCTYPE_POST;
	}

	public void setShared(int value) {
        addNumberField("shared", value);
	}
	
	public int isShared() throws DocumentException {
		return getValueAsInteger("shared");
	}
	
	public void setViewText() throws DocumentException {
		if (this.getContent() != null ) {
			String viewtext = Util.removeHTMLTags(this.getContent());
			this.setViewText(viewtext);
		}
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
	
	public void setPostDate(Date value) {
		addDateField("postdate", value);
	}
	
	public Date getPostDate() throws DocumentException{
		return getValueAsDate("postdate");
	}
	
	public String getURL() throws DocumentException {	
		return "Provider?type=edit&element=document&id=post&key=" + docID;
	}

	public int save(User user) throws DocumentAccessException, DocumentException{
		int docID = 0;
        normalizeViewTexts();
		if(isNewDoc()){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = db.getForum().insertPost(this, user);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			docID = db.getForum().updatePost(this, user);
		}
		return docID;
	}
	
	
	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		
		setDefaultRuleID(getValueForDoc(saveFieldsMap, "defaultruleid", fields).valueAsText);
		setForm(getValueForDoc(saveFieldsMap, "form", fields).valueAsText);
		setContent(getValueForDoc(saveFieldsMap, "contentsource", fields).valueAsText);
		setShared(getValueForDoc(saveFieldsMap, "shared", fields).valueAsNumber.intValue());
		setPostDate(new Date());
		
		for(ISaveField saveField: saveFieldsMap.values()){
			if (saveField.getSourceType() == ValueSourceType.WEBFORMFILE){
				blobDataProcess(saveField, fields);	
			}
		}

	}
}

