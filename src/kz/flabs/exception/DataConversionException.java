package kz.flabs.exception;

public class DataConversionException extends Exception {
	public ConversionError id;

	private static final long serialVersionUID = 5183626804447906447L;
	private String errorText;
	private Exception e;
		
	public DataConversionException(ConversionError error) {
		super();			
		processError(error);
	}
	
	private void processError(ConversionError error){
		
		switch(error){
		case CONVERSION_DATE_TO_DATABASE_DATE:
			errorText = "Unable to convert date to database date format";
			break;		
		default:
			errorText = " " + e != null ? e.toString() : "" + "";
		}	
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String toString(){
		return errorText;
	}
}
