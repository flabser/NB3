package kz.flabs.workspace;

import kz.flabs.users.User;
import kz.lof.scripting._Session;

public class LoggedUser {
	private String login;
	private String pwd;
	private User user;

	@Deprecated
	LoggedUser(User user) {
		login = user.getUserID();
		if (user.getPasswordHash() != null) {
			if (!user.getPasswordHash().trim().equals("")) {

				pwd = user.getPasswordHash();
			} else {
				pwd = user.getPassword();
			}
		} else {
			pwd = user.getPassword();
		}
		this.user = user;
	}

	public LoggedUser(_Session ses) {
		user = ses.getUser();
		login = user.getUserID();
		if (user.getPasswordHash() != null) {
			if (!user.getPasswordHash().trim().equals("")) {

				pwd = user.getPasswordHash();
			} else {
				pwd = user.getPassword();
			}
		} else {
			pwd = user.getPassword();
		}
	}

	public String getLogin() {
		return login;
	}

	public String getPwd() {

		return pwd;
	}

	public User getUser() {
		return user;
	}

}