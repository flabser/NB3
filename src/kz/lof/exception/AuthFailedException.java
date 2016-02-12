package kz.lof.exception;


@SuppressWarnings("serial")
public class AuthFailedException extends ApplicationException {

	public AuthFailedException(AuthFailedExceptionType type, String dir) {
		super(dir, type.name());
		setType("AUTHFAIL");
	}

}
