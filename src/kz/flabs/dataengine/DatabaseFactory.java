package kz.flabs.dataengine;

import kz.flabs.appenv.AppEnv;
import kz.pchelka.env.Environment;

public class DatabaseFactory implements Const {
	 
	
	public static IDatabase getDatabase(String appName){
		AppEnv appEnvironment = Environment.getApplication(appName);		
		return appEnvironment.getDataBase();
	}
	
	public static IDatabase getDatabaseByName(String dbID){
		return Environment.getDatabase(dbID);	
	}
		
	
	public static ISystemDatabase getSysDatabase(){
		return Environment.systemBase;
	}
	
}
