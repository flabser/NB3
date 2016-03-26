package kz.lof.user;

public class AnonymousUser extends SystemUser {
	public final static String USER_NAME = "anonymous";
	public final static long ID = 0;

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
