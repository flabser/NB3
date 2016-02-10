package kz.flabs.runtimeobj;

import kz.flabs.appenv.AppEnv;
import kz.flabs.users.User;

public class AccessController {
	AppEnv env;
	User user;

	AccessController(AppEnv env, User user) {

	}

	boolean isDirectAccess() {

		return false;

	}
}
