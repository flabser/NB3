package kz.nextbase.script.struct;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.runtimeobj.document.structure.UserRole;
import kz.flabs.runtimeobj.document.structure.UserRoleType;
import kz.flabs.webrule.Role;
import kz.nextbase.script._Document;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class _UserGroup extends _Document implements Const{
	public UserGroup group;

	public _UserGroup(UserGroup group, _Session ses) {
		super(group, ses);	
		this.group = group;					
	}

	public void setGroupName(String gn){
		group.setName(gn);
	}
	
	public String getName(){
		return group.getName();
	}
	
	public void setDescription(String d){
		group.setDescription(d);
	}
	
	public String getDescription(){
		return group.getDescription();
	}
	
	public void setOwner(String o){
		group.setOwner(o);
	}
	
	public String getOwner() throws _Exception{
		try {
			return group.getOwner();
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,e.getMessage());
		}
	}
	
	public void setListOfMembers(String[] t) {
		for (String member: t) {
            if (!"".equalsIgnoreCase(member)) {
                group.addMember(member);
            }
		}
	}

    public void resetMembers() {
        group.resetMembers();
    }

	public HashSet<String> getListOfMembers() {
		return (HashSet<String>) group.getMembers();
	}
	
	public void setListOfRoles(String[] t) {
		ArrayList<UserRole> newRoles = new ArrayList<>();
		if (!t[0].equals("")) {
			for (String roleName: t) {
                HashMap<String, Role> appRoles = group.env.getRolesMap();
                Role role = appRoles.get(roleName);
				if (role != null){
					role.setRuleProvider(UserRoleType.PROVIDED_BY_GROUP);
					newRoles.add(new UserRole(role));
				}			
			}
		}
		group.setRoles(newRoles);
	}
	
	public HashSet<String> getListOfRoles() {
		return group.getAllRoles();
	}

	
}
