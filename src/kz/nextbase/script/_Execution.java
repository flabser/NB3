package kz.nextbase.script;

import java.util.Date;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;

public class _Execution extends _Document {
	Execution execution;
	
	public _Execution(Execution document) {
		super(document);	
		this.execution = document;
	
	}
		
	public _Execution(Execution document, _Session ses){
		super(document, ses);	
		this.execution = document;
	}
	
	public _Execution(Execution document, String userID){
		super(document, userID);
		this.execution = document;
	}
	
	public void setFinishDate(Date d){		
		execution.setFinishDate(d);
	}
	
	public Date getFinishDate() throws _Exception{
		try {
			return execution.getFinishDate();
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage());
		}
	}
	
	public void setReport(String r){		
		execution.setReport(r);
	}
	
	public String getReport(){
		return execution.getReport();		
	}
	
	public void setExecutor(String exec){		
		execution.setExecutor(exec);
	}
	
	public String getExecutorID(){		
		return execution.getExecutor();
	}
		
	public void setNomenType(String t) throws _Exception{
		try{
			int n = Integer.parseInt(t);
			execution.setNomenType(n);
		}catch(NumberFormatException e){
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,"Value is not number, function: _Execution.setNomenType(" + t + ")");
		}
		
	}
	
	public int getNomenType(){
		return execution.getNomenType();
	}
	
	public void setNdelo(String t) throws _Exception{	
		try{
			int n = Integer.parseInt(t);
			execution.setNdelo(n);
		}catch(NumberFormatException e){
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,"Value is not number, function: _Execution.setNdelo(" + t + ")");
		}
		
	}
	
	public int getNdelo(){
		return execution.getNdelo();
	}
	
	public boolean save(){
		try {
			User user = new User(currentUserID);
			int result = execution.save(user.getAllUserGroups(), currentUserID); 
			if (result > -1){
				return true;
			}else{
				return false;	
			}		
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry(e);
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry(e);
		}
		return false;
	}
	
	public boolean save(String userID){
		try {
			User user = new User(userID);
			int result = execution.save(user.getAllUserGroups(), userID); 
			if (result > -1){
				return true;
			}else{
				return false;	
			}		
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry(e);
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry(e);
		}
		return false;
	}
}
