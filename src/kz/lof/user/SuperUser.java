package kz.lof.user;

/**
 * @author Kayra on 17/03/16.
 */

public class SuperUser extends SystemUser {
	public final static String USER_NAME = "supervisor";
	public final static long ID = -1;

	@Override
	public String getUserID() {
		return USER_NAME;
	}

	@Override
	public String getUserName() {
		return USER_NAME;
	}

	@Override
	public Long getId() {
		return (long) ID;
	}

	@Override
	public String getLogin() {
		return USER_NAME;
	}

}
