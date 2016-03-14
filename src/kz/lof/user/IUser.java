package kz.lof.user;

import kz.lof.dataengine.jpa.ISimpleAppEntity;

public interface IUser<K> extends ISimpleAppEntity<K> {

	String getPwdHash();

	String getPwd();

	String getLogin();

	boolean isAuthorized();

	void setAuthorized(boolean isAuthorized);

	String getUserID();

	String getUserName();

	void setUserName(String name);

}
