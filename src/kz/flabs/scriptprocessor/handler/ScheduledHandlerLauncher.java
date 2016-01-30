package kz.flabs.scriptprocessor.handler;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.users.User;
import kz.flabs.util.XMLResponse;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class ScheduledHandlerLauncher extends AbstractDaemon{
	
	@Override
	public int process(IProcessInitiator processOwner) {
		AppEnv env = (AppEnv) processOwner;
		User user = new User(Const.sysUser, env);
		String lang = "RUS";
		try {
			HandlerScriptProcessor hsp =  new HandlerScriptProcessor(env, user, lang);
			XMLResponse resp = hsp.processScript();	
			if (resp.resultFlag){
				return 0;
			}else{
				return -1;
			}
		} catch (Exception e) {
			Server.logger.errorLogEntry(env.appType, e);
			return -1;
		}		
	}
}
