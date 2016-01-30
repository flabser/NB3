package kz.flabs.runtimeobj.document.glossary;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.users.User;

import java.util.Date;

public class Glossary extends BaseDocument implements Const {	
	private static final long serialVersionUID = 1L;

	public Glossary(IDatabase db) {
		docType = Const.DOCTYPE_GLOSSARY;
		this.db = db;
		this.env = db.getParent();
		dbID = db.getDbID();
		setNewDoc(true);		
		setAuthor(Const.sysUser);
		currentUserID = Const.sysUser; 
	}
	
	public Glossary(AppEnv env, User currentUser) {
		docType = Const.DOCTYPE_GLOSSARY;
		this.env = env;
		this.db = env.getDataBase();
		dbID = db.getDbID();
		setNewDoc(true);
		String userID = currentUser.getUserID();
		setAuthor(userID);
		currentUserID = userID;
	}
	
	
	public String getForm(){
		return form;
	}
	
	public void setName(String name){		
		addStringField("name", name);
	}
	
	public String getName() throws DocumentException{		
		return getValueAsString("name")[0];
	}
	
	
	public void setCode(String code) {		
		addStringField("code", code);
	}
	public void setDirection(String direction){		
		addStringField("direction", direction);
	}
	
	public void setCountry(String country){		
		addStringField("country", country);
	}
	
	public void setRank(String rank) {
		addStringField("rank", rank);
	}
	
	public void setShortName(String shortname) {
		addStringField("shortname", shortname);
	}
	
	public void setCategory(String category) {
		addStringField("corrcat", category);
	}
	public void setNomenclature(String ndelo) {
		addStringField("ndelo", ndelo);
	}
	
	public void setTitle(String title) {
		addStringField("title", title);
	}
	
	public void setRang(String rang) {
		addStringField("rang", rang);
	}
	public void setRankText(String ranktext) {
		addStringField("ranktext", ranktext);
	}
	public int isRead(){		
		return 1;
	}
	

	public int save(User user) throws DocumentAccessException, DocumentException{
		int docID = 0;
        computeViewText();
        if (!normalizeViewTexts()){
			normalizeViewTexts();
		}
		
		
		if(isNewDoc()){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = db.getGlossaries().insertGlossaryDocument(this);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			if (getViewDate() == null) setViewDate(getRegDate());
			docID = db.getGlossaries().updateGlossaryDocument(this);
		}
	
		return docID;
		
	}

	

}
