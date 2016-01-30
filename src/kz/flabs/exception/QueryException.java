package kz.flabs.exception;

public class QueryException extends Exception{
	public int id;
	
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorText = "";
	
	public QueryException(QueryExceptionType error) {
		super();			
		processError(error);
	}
	

	private void processError(QueryExceptionType error){		
		switch(error){
		case RUNTIME_ERROR:
			errorText = "Runtime error";
			break;
		/*case UNKNOW_DOCUMENT_TYPE:
			errorText = "Unknown document type";
			break;
			default:
			errorText = "Error: " + error.toString() + ", field=\"" + fieldName + "\"";*/
		}	
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String toString(){
		return errorText;
	}
	
}
