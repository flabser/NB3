package kz.lof.log;

public interface ILogger {
	public void infoLogEntry(String logtext);

	public void errorLogEntry(String logtext);

	public void warningLogEntry(String logtext);

	public void debugLogEntry(String logtext);

	public void errorLogEntry(Exception exception);

	public void fatalLogEntry(String logtext);

}
