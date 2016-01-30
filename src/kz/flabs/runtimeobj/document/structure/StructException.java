package kz.flabs.runtimeobj.document.structure;


public class StructException extends Exception{
	public  StructExceptionType exceptionType;
	
	private static final long serialVersionUID = 4533277166625333467L;	
	private String errorText = "";
	private Exception e;	
	
	public StructException(StructExceptionType error) {
		super();		
		exceptionType = error;
		processError(exceptionType);
	}
	

	private void processError(StructExceptionType error){
		exceptionType = error;
		switch(exceptionType){
		case NOT_UNIQUE_USERNAME:
			errorText = "Not a unique username";
			break;	
		default:
			errorText = "Error: " + error.toString();
		}	
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String toString(){
		return errorText;
	}
}
