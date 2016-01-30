package kz.pchelka.messenger.robot;

import kz.flabs.localization.LocalizatorExceptionType;

public class RobotException extends Exception {
	public LocalizatorExceptionType id;
	
	private String fieldName;	
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorText;
	private Exception e;
	
	public RobotException(Exception e, String fieldName) {
		this.fieldName = fieldName;
		this.e = e;
		processError(LocalizatorExceptionType.RUNTIME_ERROR);
	}
	
	public RobotException(LocalizatorExceptionType error) {
		super();		
		processError(error);
	}
	
	private void processError(LocalizatorExceptionType error){
		id = error;		
		switch(id){
		case VOCABULAR_NOT_FOUND:
			errorText = "error 352=\"" + fieldName + "\"";
			break;
			default:
				errorText = "error 09: " + e.toString();
		}	
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String toString(){
		return errorText;
	}
	
}
