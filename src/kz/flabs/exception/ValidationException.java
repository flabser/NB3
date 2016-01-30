package kz.flabs.exception;

public class ValidationException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5204749199549291891L;
	private Exception realException;

	public ValidationException(String error, Exception rExp) {
		super(error);
		realException = rExp;
	}
	
	public Exception getRealException() {
		return realException;
	}


}
