package kz.lof.user;

public interface IUser {

	long getId();

	String getPwdHash();

	boolean isAuthorized();

	void setAuthorized(boolean isAuthorized);

	String getUserID();

	String getUserName();

	void setUserName(String name);

}
