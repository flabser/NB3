package kz.flabs.appdaemon;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class RemoveAttachments extends AbstractDaemon implements ICoordConst, Const {

    @Override
    public int process(IProcessInitiator processOwner) throws DocumentAccessException, RuleException, QueryFormulaParserException, QueryException {
        AppEnv env = (AppEnv) processOwner;
        try {
            IDatabase db = env.getDataBase();
            db.removeUnrelatedAttachments();
        } catch (Exception e) {
            Server.logger.errorLogEntry(env.appType, e);
            return -1;
        }
        return 0;
    }
}
