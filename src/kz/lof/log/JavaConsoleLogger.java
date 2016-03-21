package kz.lof.log;

public class JavaConsoleLogger implements ILogger {

	@Override
	public void errorLogEntry(String logtext) {
		System.err.println(logtext);
	}

	@Override
	public void errorLogEntry(Exception exception) {
		System.err.println(exception.toString());
		exception.printStackTrace();
	}

	public void errorLogEntry(String agent, String logtext) {
		System.err.println(logtext);

	}

	public void errorLogEntry(String agent, String logtext, String searchKey) {
		System.err.println(logtext + " " + searchKey);

	}

	public void errorLogEntry(String agent, Exception exception) {
		System.err.println(exception.toString());
		exception.printStackTrace();
	}

	public void errorLogEntry(String agent, Exception exception, String searchKey) {
		System.err.println(exception.toString());
		exception.printStackTrace();
	}

	@Override
	public void infoLogEntry(String logtext) {
		System.out.println(logtext);

	}

	public void infoLogEntry(String agent, String logtext) {
		System.out.println(logtext);

	}

	@Override
	public void debugLogEntry(String logtext) {
		System.out.println(logtext);

	}

	public void debugLogEntry(String agent, String logtext) {
		System.out.println(logtext);

	}

	@Override
	public void warningLogEntry(String logtext) {
		System.out.println(logtext);

	}

	public void warningLogEntry(String agent, String logtext) {
		System.out.println(logtext);

	}

	@Override
	public void fatalLogEntry(String logtext) {
		System.err.println(logtext);

	}

}
