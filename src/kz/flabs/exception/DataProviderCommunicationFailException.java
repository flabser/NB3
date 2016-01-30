package kz.flabs.exception;

public class DataProviderCommunicationFailException  extends Exception{
	private static final long serialVersionUID = 3109136561599843393L;
	private Exception realException;

	
	
	public Exception getRealException() {
		return realException;
	}

}
