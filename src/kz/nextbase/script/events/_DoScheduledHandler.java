package kz.nextbase.script.events;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.users.User;
import kz.lof.appenv.AppEnv;
import kz.lof.scripting._Session;
import kz.lof.user.AnonymousUser;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;

public abstract class _DoScheduledHandler extends AbstractDaemon {

	@Override
	public int process(IProcessInitiator processOwner) throws DocumentAccessException, RuleException, QueryFormulaParserException {
		AppEnv env = (AppEnv) processOwner;
		User user = new User(Const.supervisorGroup[0], env);
		_Session session = new _Session(env, new AnonymousUser());
		String lang = "RUS";
		return doHandler(session);
	}

	public abstract int doHandler(_Session session);

}
