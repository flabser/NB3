package kz.flabs.exception;

public class ComplexObjectException extends Exception{
	public int id;
	public  ComplexObjectExceptionType exceptionType;
	
	private static final long serialVersionUID = 2813324459147403494L;
	private String fieldName, addInfo;	
	private String errorText = "";
	private Exception e;	
	
	public ComplexObjectException(Exception e, String fieldName) {
		this.fieldName = fieldName;
		this.e = e;
		processError(ComplexObjectExceptionType.RUNTIME_ERROR);
	}
		
	
	public ComplexObjectException(ComplexObjectExceptionType error, String addInfo) {
		super();
		exceptionType = error;
		this.addInfo = addInfo;	
		processError(exceptionType);
	} 
	
	public ComplexObjectException(ComplexObjectExceptionType error) {
		super();		
		exceptionType = error;
		processError(exceptionType);
	}
	
	private void processError(ComplexObjectExceptionType error){
		exceptionType = error;
		switch(exceptionType){
		case FIELD_NOT_FOUND:
			errorText = "Field not found, field=\"" + fieldName + "\"";
			break;
		case RUNTIME_ERROR:
			errorText = "Runtime error";
			break;
		case PARSER_ERROR:
			errorText = "Parser of complex object has caused error: " + addInfo;
			break;
		case COMPLEX_VALUE_INCORRECTCT:
			errorText = "Parser has found error in complex value";
			break;	
		case CANNOT_CAST_TO_CLASS:
			errorText = "Provided class name is has not initiated field value: " + addInfo;
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
