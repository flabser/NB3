package kz.pchelka.digitalsign;

public class Log4jLoggerApplet {
    public org.apache.log4j.Logger log4jLogger;

    private String module = "";
   
    public Log4jLoggerApplet(String module) {
        log4jLogger = org.apache.log4j.Logger.getLogger(module);
        this.module = module;
    }

    public void errorLogEntry(String logtext) {
        log4jLogger.error(logtext);        
    }

    public void fatalLogEntry(String logtext) {
        log4jLogger.fatal(logtext);       
    }
    
    public void errorLogEntry(Exception exception) {
        log4jLogger.error(exception);   

        exception.printStackTrace();
    }

    public void errorLogEntry(String agent, String logtext) {
        log4jLogger.error(agent + "-" + logtext);      
    }

    public void errorLogEntry(String agent, String logtext, String searchKey) {
        log4jLogger.error(agent + "-" + logtext + " searchKey=" + searchKey);
    }

    public void errorLogEntry(String agent, Exception exception) {
        String msg = agent + "-" + exception.toString();
        msg += getErrorStackString(exception.getStackTrace());
        log4jLogger.error(msg, exception);       
    }

    public void errorLogEntry(String agent, Exception exception, String searchKey) {
        String msg = exception.toString();
        msg += getErrorStackString(exception.getStackTrace());
        log4jLogger.error(agent + " searchKey=" + searchKey + " " + msg, exception);
   
    }

    public void normalLogEntry(String logtext) {
        log4jLogger.info(logtext);
    }

    public void normalLogEntry(String agent, String logtext) {
        log4jLogger.info(agent + "-" + logtext);
    }

    public void verboseLogEntry(String logtext) {
           log4jLogger.debug(logtext);        
    }

    public void verboseLogEntry(String agent, String logtext) {
          log4jLogger.debug(agent + "-" + logtext);
    }

    public void warningLogEntry(String agent, String logtext) {
        log4jLogger.info(agent + "-" + logtext);
    }

    public void warningLogEntry(String logtext) {
        log4jLogger.warn(logtext);
    }

    public boolean getLoggingMode() {
        return false;
    }

    public static String getErrorStackString(StackTraceElement stack[]) {
        StringBuffer addErrorMessage = new StringBuffer(1000);
        for (int i = 0; i < stack.length; i++) {
            addErrorMessage.append("\n" + stack[i].getClassName()
                    + " > " + stack[i].getMethodName() + " "
                    + Integer.toString(stack[i].getLineNumber()) + "\n");
        }

        return addErrorMessage.toString();
    }
}
