package kz.flabs.runtimeobj.document.structure;

import java.util.ArrayList;
import java.util.HashMap;
import kz.flabs.webrule.Role;


public class UserRoleCollection {
	private HashMap <String, Role> rolesMap = new HashMap <String, Role>();
	private ArrayList <Role> rolesList = new ArrayList<Role>();
	
	
	public boolean put(Role role){		
		if(!rolesMap.containsKey(role)){
			rolesMap.put(role.name + "#" + role.appID, role);	
			rolesList.add(role);
			return true;
		}else{
			return false;
		}
	}

	public HashMap <String, Role> getRolesMap() {
		return rolesMap;
	}
	
	public ArrayList<Role> getRolesList() {
		return rolesList;
	}
	
		
	
}
