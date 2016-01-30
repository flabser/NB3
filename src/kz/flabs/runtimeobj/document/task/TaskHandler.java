package kz.flabs.runtimeobj.document.task;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLResponse;

public class TaskHandler implements Const{
	private UserSession userSession;
	private AppEnv env;
	private IDatabase db;
	
	public TaskHandler(AppEnv env, String key, UserSession user){	
		db = env.getDataBase();
		this.env = env;
		this.userSession = user;
		int docID = Integer.parseInt(key);

	}
	
	public XMLResponse resetExecutor(){
		XMLResponse result = new XMLResponse(ResponseType.RESET_EXECUTOR);
		
		/*if (prj.isValid){
			
		}*/
		
		return result;
		
	}

	
}
