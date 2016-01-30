package kz.nextbase.script.task;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.Executor;
import kz.nextbase.script._Database;
import kz.nextbase.script._Exception;
import kz.nextbase.script._Glossary;
import kz.nextbase.script._Session;

import java.util.Date;

public class _Executor {
    private _Session ses;
    private Executor exec;

    public _Executor(_Session ses, Executor exec) {
        this.exec = exec;
        this.ses = ses;
    }

    public _Executor(String id, Executor.ExecutorType type) {
        Executor exec = new Executor(id, type);
        this.exec = exec;
    }

    public void createInternalExecutor(String id) {
        Executor executor = new Executor(id, Executor.ExecutorType.INTERNAL);
        this.exec = executor;
    }

    public void createExternalExecutor(String id) {
        Executor executor = new Executor(id, Executor.ExecutorType.EXTERNAL);
        this.exec = executor;
    }

    public String getUserID() throws DocumentException {
        return exec.getID();
    }

    public void setReset(boolean isReset) {
        exec.isReset = isReset;
    }

    public String getShortName() {
        try {
            switch (exec.type) {
                case EXTERNAL:
                    _Database db = ses.getCurrentDatabase();
                    _Glossary doc = db.getGlossaryByID(exec.getID());
                    return doc.getName();
                case INTERNAL:
                    IStructure struct = ses.getCurrentDatabase().getBaseObject().getStructure();
                    Employer emp = struct.getAppUser(exec.getID());
                    return emp.getShortName();
            }
            return "";
        } catch (_Exception e) {
            AppEnv.logger.errorLogEntry(e.getMessage());
            return "";
        }
    }

    public int getPercentOfExecution() {
        return exec.getPercentOfExecution();
    }

    public void setPercentOfExecution(int percent) {
        exec.setPercentOfExecution(percent);
    }

    public int getResponsible() {
        return exec.getResponsible();
    }

    public String getResetAuthorID() {
        return exec.resetAuthorID;
    }

    public void setResetAuthorID(String userID) {
        exec.resetAuthorID = userID;
    }

    public Date getResetDate() {
        return exec.getResetDate();
    }

    public void setResetDate(Date endDate) {
        exec.setResetDate(endDate);
    }

    Executor getBaseObject() {
        return exec;
    }

    public String toString() {
        return exec.toString();
    }

}
