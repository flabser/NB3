package kz.flabs.localization;

@Deprecated
public class LocalizatorException extends Exception {
	public LocalizatorExceptionType id;

	private String fieldName;
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorText;
	private Exception e;

	public LocalizatorException(Exception e, String fieldName) {
		this.fieldName = fieldName;
		this.e = e;
		processError(LocalizatorExceptionType.RUNTIME_ERROR);
	}

	public LocalizatorException(LocalizatorExceptionType error) {
		super();
		processError(error);
	}

	private void processError(LocalizatorExceptionType error) {
		id = error;
		switch (id) {
		case VOCABULAR_NOT_FOUND:
			errorText = "vocabular not found";
			break;
		default:
			errorText = e.toString();
		}
	}

	@Override
	public String getMessage() {
		return errorText;
	}

	@Override
	public String toString() {
		return errorText;
	}

}
