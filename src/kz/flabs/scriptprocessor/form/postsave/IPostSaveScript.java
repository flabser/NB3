package kz.flabs.scriptprocessor.form.postsave;

import kz.flabs.appenv.AppEnv;
import kz.nextbase.script.*;

public interface IPostSaveScript {	
	void setSession(_Session ses);	
	void setDocument(_Document doc);
	@Deprecated
	void setUser(String user);	
	void setAppEnv(AppEnv env);	
	void process();
}
