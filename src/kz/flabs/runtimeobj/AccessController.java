package kz.flabs.runtimeobj;

import kz.flabs.appenv.AppEnv;
import kz.flabs.users.User;
import kz.flabs.webrule.query.QueryRule;

public class AccessController {
	AppEnv env;
	User user;
	QueryRule rule;
	
	AccessController(AppEnv env, User user){
		
	}
	
	boolean isDirectAccess(){
		            
		
		return false;
		
	}
}
