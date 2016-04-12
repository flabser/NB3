package kz.lof.user;

import java.util.List;

import kz.lof.administrator.model.Application;
import kz.lof.dataengine.jpa.ISimpleAppEntity;
import kz.lof.localization.LanguageCode;

public interface IUser<K> extends ISimpleAppEntity<K> {

	String getPwdHash();

	String getPwd();

	String getLogin();

	boolean isAuthorized();

	void setAuthorized(boolean isAuthorized);

	String getUserID();

	String getUserName();

	void setUserName(String name);

	boolean isSuperUser();

	List<Application> getAllowedApps();

	void setRoles(List<String> allRoles);

	List<String> getRoles();

	LanguageCode getDefaultLang();

}
