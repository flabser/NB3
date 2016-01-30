package kz.flabs.exception;

public class CheckResponsesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4319515826853095404L;
	private Exception realException;
	
	public CheckResponsesException(String error, Exception rException) {
		super(error);
		realException = rException;
	}
	
	public Exception getRealException() {
		return realException;
	}	
	
}
