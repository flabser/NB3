package kz.flabs.scriptprocessor.form.querysave;

import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Document;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;

@Deprecated
public abstract class AbstractQuerySaveScript implements IQuerySaveScript {	
	private _Session ses;
	private _Document doc;
	private String user;
	private boolean continueSave = true;
	private String msg = "";
	@Deprecated
	private String redirectView = "";
	private Vocabulary vocabulary;
	private String lang;
	private String redirectPage;
	
	public void setSession(_Session ses){			
		this.ses = ses;
	}
		
	public void setDocument(_Document doc){
		this.doc = doc;
	}
	

	@Override
	public void setWebFormData(_WebFormData webFormData) {
		
		
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public void setCurrentLang(Vocabulary vocabulary, String lang){
		this.lang = lang;
		this.vocabulary = vocabulary;
	}
	
	public void stopSave(){
		this.continueSave = false;
	}
	
	@Deprecated
	public void setRedirectView(String redirectView){
		this.redirectView = redirectView;
	}
	
	public void setRedirectPage(String redirectPage){
		this.redirectPage = redirectPage;
	}
	
	public QuerySaveResult process(){
		try{
			doQuerySave(ses, doc, user);		
		}catch(Throwable e){
			continueSave = new Boolean(false);
			e.printStackTrace();
			msg = e.getMessage() + ", " + getGroovyError(e.getStackTrace());
			//msg = "QuerySaveScript: " + e.getClass().getSimpleName() + " " + e.getMessage();
		}
		QuerySaveResult qsr = new QuerySaveResult(continueSave, msg, redirectView, redirectPage);
		return qsr;	
	}
	
	public void msgBox(String m){
		msg = m;
	}
	
	public void localizedMsgBox(String m){
		msg = vocabulary.getWord(m, lang)[0];	
	}
	
	public static String getGroovyError(StackTraceElement stack[]){		
		for (int i=0; i<stack.length; i++){
			if (stack[i].getClassName().contains("Foo")){
				return stack[i].getMethodName()+", > "+Integer.toString(stack[i].getLineNumber() - 3) + "\n";	
			}
		}
		return "";
	}
		
	@Deprecated
	public abstract void doQuerySave(_Session ses, _Document doc, String user);
		
}
