package kz.lof.user;

public class AnonymousUser implements IUser<Long> {
	public final static String USER_NAME = "anonymous";

	@Override
	public String getPwdHash() {
		return null;
	}

	@Override
	public boolean isAuthorized() {
		return true;
	}

	@Override
	public void setAuthorized(boolean isAuthorized) {

	}

	@Override
	public String getUserID() {
		return USER_NAME;
	}

	@Override
	public String getUserName() {
		return USER_NAME;
	}

	@Override
	public void setUserName(String name) {

	}

	@Override
	public void setId(Long id) {

	}

	@Override
	public Long getId() {
		return null;
	}

}
