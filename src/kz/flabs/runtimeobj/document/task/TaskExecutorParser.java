package kz.flabs.runtimeobj.document.task;

import kz.flabs.exception.WebFormValueException;
import kz.flabs.util.Util;

import java.util.Date;
import java.util.StringTokenizer;

public class TaskExecutorParser {
	
	public Executor parse(Task task, String complexString) throws WebFormValueException{
	//	Executor taskExecutor = new Executor(task);
		Executor taskExecutor = null;
		StringTokenizer t = new StringTokenizer(complexString,"`");
		while(t.hasMoreTokens()){
			taskExecutor.setID(t.nextToken());
			taskExecutor.setResponsible(Integer.valueOf(t.nextToken()));
			taskExecutor.setPercentOfExecution(Integer.valueOf(t.nextToken()));
			try{	
				Date resetDate = Util.convertStringToDateTimeSilently(t.nextToken());				 
				if (resetDate != null){
					taskExecutor.setResetDate(resetDate);
					taskExecutor.resetAuthorID = t.nextToken();
					taskExecutor.isReset = true;
				}else{		
					taskExecutor.isReset = false;
				}
			}catch (java.util.NoSuchElementException nse){
				taskExecutor.isReset = false;
			}
		}

		return taskExecutor;
	}

}
