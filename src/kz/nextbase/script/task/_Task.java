package kz.nextbase.script.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import kz.flabs.dataengine.DatabaseConst;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Executor;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;
import kz.nextbase.script._Document;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._Session;
import kz.nextbase.script.constants._TaskType;
import kz.nextbase.script.task._Executor;

public class _Task extends _Document {
	private Task task;
	
	public _Task(BaseDocument doc, _Session session) {
		super(doc, session);		
		this.task = (Task) doc;
	}
	
	public _Task(Task document, String user) {
		super(document, user);		
		this.task = document;
	}
		
	public _Task(Task document, _Session ses){
		super(document, ses);		
		this.task = document;
	}
	
	public _Task(Task document) {
		super(document);
		this.task = document;
	}
	
	public Task getBaseObject(){
		return task;
	}
	
	public String getContent(){
		return task.getContent();
	}
	
	public _Control getControl() {
		return new _Control(null, task.getControl());
	}

	public void clearAllExecutors(){
		task.clearAllExecutors();
	}

	public void setComment(String comment){		
		this.task.setComment(comment);
	}
	
	public String getComment(){
		return task.getComment();
	}
	

	public void attachControl(_Control ctrl) {
		task.setControl(ctrl.getBaseObject());
	}
	
	@Deprecated
	public void setControl(Control ctrl) {
		task.setControl(ctrl);
	}
	
	@Deprecated
	public int getCycleControl(){
		Control ctrl = this.task.getControl();
		return ctrl.getCycle();
	}
	
	@Deprecated
	public Date getCtrlDate(){
		Control ctrl = this.task.getControl();
		return ctrl.getExecDate();
	}
	
	public _Executor getExecutor(String execID){
		Executor exec = task.getExecutor(execID);
		if (exec != null){
			return new _Executor(getSession(), exec);
		}else{
			return null;
		}
	}
	
	@Deprecated
	public void setCtrlDate(Date ctrlDate){
		Control ctrl = this.task.getControl();
		ctrl.setPrimaryCtrlDate(ctrlDate);
	}
	
	public String getTaskVn() {
		return this.task.getTaskVn();
	}
	
	public void setTaskVn(String vn) {
		this.task.setTaskVn(vn);
	}
	
	public Date getTaskDate() throws _Exception{	
		try {
			return task.getTaskDate();
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Task.getTaskDate()");
		}
	}
	
	public void setTaskDate(Date dateRes){	
		this.task.setTaskDate(dateRes);
	}
	
	public void setTaskAuthor(String authorRes){		
		this.task.setTaskAuthor(authorRes);
	}
	
	public String getTaskAuthor(){
		return task.getTaskAuthor();
	}
	
	public void setCycleControl(int cycle){
		Control ctrl = this.task.getControl();
		ctrl.setCycle(cycle);
	}
	
	public void setContent(String content) {
		this.task.setContent(content);
	}
	
		
	public String getBriefContent(){
		return this.task.getBriefContent();
	}
	
	public void setBriefContent(String briefcontent){
		this.task.setBriefContent(briefcontent);
	}
	
	public  Collection<_Executor> getExecutorsList(){
		Collection<_Executor> executors = new ArrayList<_Executor>();
		Collection<Executor> execs = task.getExecutorsList();
		for(Executor exec: execs){
			executors.add(new _Executor(getSession(), exec));
		}
		return executors;
	}
	
	public void setControl(boolean c){
		Control ctrl = task.getControl();
		if (c){
			ctrl.setAllControl(DatabaseConst.ALLCONTROL_ONCONTROL);
		}else{
			ctrl.setAllControl(DatabaseConst.ALLCONTROL_RESET);
		}
	}
	
	public void addExecutor(_Executor exec){
		task.addExecutor(exec.getBaseObject());
	}
	
	public void setOld(int old){
		Control ctrl = task.getControl();
		ctrl.setOld(old);
	}
	
	public int isOld(){
		Control ctrl = task.getControl();
		return ctrl.getOld();
	}
	
	@Deprecated
	public int getResolType(){
		return task.getResolTypeAsInt();
	}
	
	public _TaskType getTaskType(){
		int resolType = task.getResolTypeAsInt();
		if (resolType == 1){
			return _TaskType.RESOLUTION;
		}else if (resolType == 2){
			return _TaskType.CONSIGN;
		}else if (resolType == 3){
			return _TaskType.TASK;
		}
		return _TaskType.UNKNOWN;
	}
	
	public boolean isControl(){
		return (task.getControl().getAllControl() == DatabaseConst.ALLCONTROL_ONCONTROL);		
	}
	
	public String getValueString(String fieldName){
		try {	
			String val[] = {""};
			val = task.getValueAsString(fieldName);
			return val[0];
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(e.getMessage() + ", function: _Task.getValueString(" + fieldName + "), returned:\"\"");
			return "";
		}
	}
	
	public boolean save(){
		try {
			User user = new User(currentUserID);
			int result = task.save(user.getAllUserGroups(), currentUserID); 
			if (result > -1){
				return true;
			}else{
				return false;	
			}		
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentAccessException " + e.getMessage() + ", function: _Task.save(), returned:false");
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentException " + e.getMessage() + ", function: _Task.save(), returned:false");
		}
		return false;
	}
	
	public boolean save(String userID){
		try {
			User user = new User(userID);
			int result = task.save(user.getAllUserGroups(), userID); 
			if (result > -1){
				return true;
			}else{
				return false;	
			}	
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentAccessException " + e.getMessage() + ", function: _Task.save(" + userID + "), returned:false");
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentException " + e.getMessage() + ", function: _Task.save(" + userID + "), returned:false");
		}
		return false;
	}
	
	public void setProject(int value) {
		this.task.setProject(value);
	}
	
	public void setCategory(int value) {
		this.task.setCategory(value);
	}
	
	public int getCategory() {
		return task.getCategory();
	}
	
	
}
