package kz.lof.user;

/**
 * @author Kayra on 17/03/16.
 */

public abstract class SystemUser implements IUser<Long> {

	@Override
	public String getPwdHash() {
		return null;
	}

	@Override
	public String getPwd() {
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
	public abstract String getUserID();

	@Override
	public abstract String getUserName();

	@Override
	public void setUserName(String name) {

	}

	@Override
	public void setId(Long id) {

	}

	@Override
	public abstract Long getId();

	@Override
	public abstract String getLogin();

}
