package kz.flabs.exception;

@Deprecated
public class ViewException extends Exception {

	
	private static final long serialVersionUID = 4762010135613823296L;
	private Exception realException;
	
	public ViewException(String error) {
		super(error);

		
		realException = this;
	}
	
	public Exception getRealException() {
		return realException;
	}
	
}
