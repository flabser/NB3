package kz.flabs.exception;

public class XMLFileNotFoundException  extends Exception {
	private static final long serialVersionUID = 179075118057606604L;
	private Exception realException;

	public XMLFileNotFoundException(String xmlFile) {
		super("xml file " + xmlFile + ", not found");
	}
	
	public Exception getRealException() {
		return realException;
	}
}
