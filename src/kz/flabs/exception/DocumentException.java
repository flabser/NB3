package kz.flabs.exception;

public class DocumentException extends Exception{
	public int id;
	public  DocumentExceptionType exceptionType;
	
	private String fieldName;	
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorText = "";
	private Exception e;	
	
	public DocumentException(Exception e, String fieldName) {
		this.fieldName = fieldName;
		this.e = e;
		processError(DocumentExceptionType.RUNTIME_ERROR);
	}
		
	
	public DocumentException(DocumentExceptionType error, String fieldName) {
		super();
		exceptionType = error;
		this.fieldName = fieldName;	
		processError(exceptionType);
	} 
	
	public DocumentException(DocumentExceptionType error) {
		super();		
		exceptionType = error;
		processError(exceptionType);
	}
	

	private void processError(DocumentExceptionType error){
		exceptionType = error;
		switch(exceptionType){
		case FIELD_NOT_FOUND:
			errorText = "Field not found, field=\"" + fieldName + "\"";
			break;
		case RUNTIME_ERROR:
			errorText = "Runtime error";
			break;
		case UNKNOW_DOCUMENT_TYPE:
			errorText = "Unknown document type";
			break;
		case ERROR_RECOVERY_PROCESS:
			errorText = "The linked document cannot be found";
			break;
		case VALUE_TOO_LONG:
			errorText = "Value too long, field=\"" + fieldName + "\"";
			break;
		default:
			errorText = "Error: " + error.toString() + ", field=\"" + fieldName + "\"";
		}	
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String toString(){
		return errorText;
	}
	
}
