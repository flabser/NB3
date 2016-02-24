package kz.flabs.dataengine;

import java.util.ArrayList;
import java.util.HashMap;

import kz.flabs.runtimeobj.viewentry.IViewEntryCollection;
import kz.flabs.users.User;
import kz.lof.dataengine.system.IEmployeeDAO;

public interface ISystemDatabase {
	User checkUser(String userID, String pwd, User user);

	User checkUser(String userID, String pwd, String hash, User user);

	User checkUserHash(String userID, String pwd, String hash, User user);

	User getUser(long docID);

	User getUser(String userID);

	User reloadUserData(User user, String userID);

	User reloadUserData(User user, int hash);

	int update(User user);

	int insert(User user);

	ArrayList<User> getAllUsers(String condition, int start, int end);

	IViewEntryCollection getUsers(String condition, int start, int end);

	int getUsersCount(String condition);

	int getAllUsersCount(String condition);

	HashMap<String, User> getAllAdministrators();

	boolean deleteUser(int docID);

	ArrayList<User> getUsers(String keyWord);

	void removeAppEntry(User user);

	void setEmployeeDAO(IEmployeeDAO dao);

}
