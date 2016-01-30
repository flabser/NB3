package kz.flabs.scriptprocessor;

import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.users.User;
import kz.nextbase.script._Document;
import kz.nextbase.script._Glossary;
import kz.nextbase.script._Session;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.task._Task;

public class DocumentScriptProcWithLang  extends ScriptProcessor{
	private _Document doc;
	private String lang;

	public DocumentScriptProcWithLang(BaseDocument d, User user, String lang){
		super();
		this.lang = lang;
		_Session ses = new  _Session(d.db.getParent(),user,this);		
		if (d instanceof Project){			
			doc = new _Project((Project)d, ses);
		}else if (d instanceof Task){
			doc = new _Task((Task)d, ses);
		}else if (d instanceof Glossary){
			doc = new _Glossary((Glossary)d, ses);
		}else{
			doc = new _Document((Document)d, ses);
		}		
	}
	

	public String[] processString(String script) {
		try{
			IScriptSource myObject = setScriptLauncher(script, false);	
			myObject.setDocument(doc);
			myObject.setLang(lang);
			String[] resObj = myObject.documentLangProcess();
			if (resObj!= null){
				return resObj;
			}else{
				return ScriptSource.getBlankValue();
			}
		}catch(Exception e){
			ScriptProcessor.logger.errorLogEntry(script);
			ScriptProcessor.logger.errorLogEntry(e);
			return null;
		}
	}
	
	public String toString(){
		return "type:" + ScriptProcessorType.DOCUMENT;
	}


}
