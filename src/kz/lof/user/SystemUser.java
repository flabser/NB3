package kz.lof.user;

import java.util.ArrayList;
import java.util.List;

import kz.lof.administrator.model.Application;

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
	public boolean isSuperUser() {
		return false;
	}

	@Override
	public List<Application> getAllowedApps() {
		return new ArrayList<Application>();
	}

	@Override
	public void setRoles(List<String> allRoles) {

	}

	@Override
	public List<String> getRoles() {
		return new ArrayList<String>();
	}

	@Override
	public abstract Long getId();

	@Override
	public abstract String getLogin();

}
