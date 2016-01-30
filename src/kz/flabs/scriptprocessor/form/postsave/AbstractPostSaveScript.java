package kz.flabs.scriptprocessor.form.postsave;

import kz.flabs.appenv.AppEnv;
import kz.nextbase.script._Document;
import kz.nextbase.script._Session;

@Deprecated
public abstract class AbstractPostSaveScript implements IPostSaveScript {	
	private _Session ses;
	private _Document doc;
	private String user;
	
	public void setSession(_Session ses){			
		this.ses = ses;
	}
		
	public void setDocument(_Document doc){
		this.doc = doc;
	}
	
	@Deprecated
	public void setUser(String user){
		this.user = user;
	}
	
	public void setAppEnv(AppEnv env){
		
	}
	
	public void process(){
		try{
			doPostSave(ses, doc, user);		
		}catch(Exception e){
		
		}	
	}
	
	
		
	public abstract void doPostSave(_Session ses, _Document doc, String user);
	
	
}
