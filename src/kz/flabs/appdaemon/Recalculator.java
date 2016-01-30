package kz.flabs.appdaemon;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IProjects;
import kz.flabs.dataengine.ITasks;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class Recalculator  extends AbstractDaemon {


	@Override
	public int process(IProcessInitiator processOwner) {
		AppEnv env = (AppEnv) processOwner;
		try{
			IDatabase db = env.getDataBase();
			ITasks t = db.getTasks();
			t.recalculate();
			IProjects p = db.getProjects();
			p.recalculate();
			return 0;
		}catch(Exception e){
			Server.logger.errorLogEntry(env.appType, e);
			return -1;
		}
	}
	
	
}
