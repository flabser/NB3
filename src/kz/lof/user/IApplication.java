package kz.lof.user;

public interface IApplication {

	long getId();

	String getPwdHash();

	boolean isAuthorized();

	void setAuthorized(boolean isAuthorized);

	String getUserID();

	String getUserName();

}
