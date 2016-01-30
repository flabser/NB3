package kz.flabs.exception;


public class LicenseException extends Exception {
	public int id;
	public String user;
	
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorTextRus;
	
		
	public LicenseException(LicenseExceptionType error) {
		super();	
		switch(error){ 
		case NUMBER_OF_LICENSE_HAS_ENDED:		
			errorTextRus = "User license is out";
			break;		
		
		}		
	}
	
	public String getMessage(){
		return errorTextRus;
	}
	
	public String toString(){
		return errorTextRus;
	}
	
}
