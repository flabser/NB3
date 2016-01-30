package kz.flabs.workspace;

import java.nio.charset.Charset;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import kz.flabs.users.User;
import kz.flabs.util.Util;

public class WorkSpaceSession {
	private static HashMap<Integer,LoggedUser> loggedUsers = new HashMap<Integer,LoggedUser>();
	
	public static HashMap<Integer,LoggedUser> getLoggedUsers() {
		return loggedUsers;
	}
	
	public static String addUserSession(User user){
		String sesID = Util.generateRandomAsText();
		LoggedUser loggetUser = new LoggedUser(user);
		int key = Base64.encodeBase64String(loggetUser.getLogin().getBytes(Charset.forName("UTF-8"))).hashCode();
		loggedUsers.put(key, loggetUser);
		return sesID + "#" + key ;
	}
	
	public static LoggedUser getLoggeedUser(String token) {
		int key = 0;
		try{
			key = Integer.parseInt(token.substring(token.indexOf("#") + 1, token.length()));
		}catch (NumberFormatException e){

		}
		LoggedUser loggetUser = loggedUsers.get(key);
		return loggetUser;
	}	

}
