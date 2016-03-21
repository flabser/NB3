package kz.lof.log;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import kz.lof.env.Environment;

public class Log4jLogger implements ILogger {
	public org.apache.log4j.Logger log4jLogger;

	public Log4jLogger(String module) {
		log4jLogger = org.apache.log4j.Logger.getLogger(module);
	}

	@Override
	public void errorLogEntry(String logtext) {
		log4jLogger.error(logtext);

	}

	@Override
	public void fatalLogEntry(String logtext) {
		log4jLogger.fatal(logtext);

	}

	@Override
	public void errorLogEntry(Exception exception) {
		log4jLogger.error(exception);

		exception.printStackTrace();
	}

	@Override
	public void infoLogEntry(String logtext) {
		log4jLogger.info(logtext);

	}

	@Override
	public void debugLogEntry(String logtext) {
		if (Environment.verboseLogging) {
			log4jLogger.debug(logtext);
		}
	}

	@Override
	public void warningLogEntry(String logtext) {
		log4jLogger.warn(logtext);

	}

	public boolean getLoggingMode() {
		return false;
	}

	public static String getErrorStackString(StackTraceElement stack[]) {
		StringBuffer addErrorMessage = new StringBuffer(1000);
		for (int i = 0; i < stack.length; i++) {
			addErrorMessage.append("\n" + stack[i].getClassName() + " > " + stack[i].getMethodName() + " "
			        + Integer.toString(stack[i].getLineNumber()) + "\n");
		}

		return addErrorMessage.toString();
	}

	public String getBuildDateTime() {
		String value = "";
		JarFile jarFile = null;
		try {
			String pathToJar = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			if (pathToJar.contains("jar")) {
				jarFile = new JarFile(new File(pathToJar));
				Manifest entry = jarFile.getManifest();
				Attributes attrs = entry.getMainAttributes();
				value = attrs.getValue("Built-Date");
			}
		} catch (Exception e) {
			errorLogEntry(e);
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					errorLogEntry(e);
				}
			}
		}
		return value;
	}

}
