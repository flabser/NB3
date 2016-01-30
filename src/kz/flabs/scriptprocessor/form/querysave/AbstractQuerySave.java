package kz.flabs.scriptprocessor.form.querysave;

import kz.flabs.localization.Vocabulary;
import kz.flabs.scriptprocessor.ScriptEvent;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.nextbase.script._Document;
import kz.nextbase.script._Exception;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

public abstract class AbstractQuerySave extends ScriptEvent implements IQuerySaveScript {	
	private _Session ses;
	private _Document doc;
	private boolean continueSave = true;
	private String msg = "";
	@Deprecated
	private String redirectView = "";
	private String lang;
	private _WebFormData webFormData;

	public void setSession(_Session ses){			
		this.ses = ses;
	}

	public void setDocument(_Document doc){
		this.doc = doc;
		ses.setDocumentInConext(doc);
	}


	@Override
	public void setWebFormData(_WebFormData webFormData) {
		this.webFormData = webFormData;
	}

	@Deprecated
	public void setUser(String user){

	}

	public void setCurrentLang(Vocabulary v, String l){
		lang = l;
		vocabulary = v;
	}

	public void stopSave(){
		this.continueSave = false;
	}

	@Deprecated
	public void setRedirectView(String redirectView){
		this.redirectView = redirectView;
	}

	public QuerySaveResult process(){
		try{
			doQuerySave(ses, doc, webFormData, lang);		
		}catch(Throwable e){
			continueSave = new Boolean(false);
			if (e instanceof _Exception){
				_Exception _e = (_Exception)e;

				msg = _e.getLocalizedMessage();
			}else{
				String wfd = webFormData.toString();
				if (wfd.length() > 50) wfd = wfd.substring(0,250) + "...";
				msg = e + ", " + getGroovyError(e.getStackTrace()) + ", webformdata:" + wfd;
			}
			ScriptProcessor.logger.errorLogEntry(e);
		}
		QuerySaveResult qsr = new QuerySaveResult(continueSave, msg, redirectView, redirectURL);
		return qsr;	
	}

	public void msgBox(String m){
		msg = m;
	}

	public void localizedMsgBox(String m){
		msg = vocabulary.getWord(m, lang)[0];	
	}


	public abstract void doQuerySave(_Session ses, _Document doc, _WebFormData webFormData, String lang);


}
