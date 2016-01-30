package kz.flabs.dataengine.postgresql.alter;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.DatabasePoolException;

public class CheckDataBase extends kz.flabs.dataengine.h2.alter.CheckDataBase {
		
		public CheckDataBase(AppEnv env) throws InstantiationException, IllegalAccessException, ClassNotFoundException, DatabasePoolException {	
			super(env, true);
            Updates u = new Updates();
            setUpdates(u);
		}

}