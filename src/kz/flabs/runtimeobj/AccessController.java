package kz.flabs.runtimeobj;

import kz.flabs.users.User;
import kz.lof.appenv.AppEnv;

public class AccessController {
	AppEnv env;
	User user;

	AccessController(AppEnv env, User user) {

	}

	boolean isDirectAccess() {

		return false;

	}
}
