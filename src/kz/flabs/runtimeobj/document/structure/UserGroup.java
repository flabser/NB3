package kz.flabs.runtimeobj.document.structure;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.form.ISaveField;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
public class UserGroup extends Document {
	protected transient IStructure struct;
	
	private static final long serialVersionUID = 1L;
	private String name;
	private String owner;
	private String description;
	private Set<String> members = new HashSet<String>();
	private HashSet<UserRole> roles = new HashSet<UserRole>();

	public UserGroup(IStructure struct){
		super(struct.getParent(), Const.sysUser);
		this.struct = struct;
		docType = DOCTYPE_GROUP;
	}

	public String getName(){
		return this.name;
	}

	public void setOwner(String emp){
		owner = emp;
        addStringField("owner", emp);
	}

	public String getOwner() throws DocumentException{
		return owner;
	}

	public void setName(String name){
		if (!"".equalsIgnoreCase(name) && name != null) {
			if (!name.startsWith("[")) {
				if (name.length() <= 31) {
					name = "[" + name;
				} else {
					name = "[" + name.substring(0, 31);
				}
			}
			if (!name.endsWith("]")) {
				if (name.length() <= 31) {
					name = name + "]";
				} else {
					name = name.substring(0, 31) + "]";
				}				
			}
			this.name = name;
			addStringField("name", name);
		}		
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
		addStringField("description", description);
	}

	public void addRole(UserRole role) {
		if (!roles.contains(role)) {
			roles.add(role);
			addValueToList("role", role.getName());
		}		
	}

	public void addMember(String user){
		members.add(user);
		addValueToList("members", user);
	}

	public void deleteMember(String user){
		members.remove(user);
	}

	public void resetMembers() {
		members.clear();
	}
	
	public Set<String> getMembers(){
		return members;
	}

	public void deleteRole(UserRole role) {
		roles.remove(role);
	}

	public HashSet<String> getAllRoles(){
		HashSet<String> roles = new HashSet<String>();
		for (UserRole role : this.getRoles()) {
			roles.add(role.getName());
		}
	
		return roles;
	}
	
	public HashSet<UserRole> getRoles() {
		return roles;
	}

	public boolean hasRole(String roleName) {
		for (UserRole role : roles) {
			if (role.getName().equalsIgnoreCase(roleName)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasMember(Employer emp) {
		return members.contains(emp);
	}

	public boolean hasRole(UserRole role) {
		return roles.contains(role);
	}

	public void setRoles(ArrayList<UserRole> newRoles) {		
		this.roles.clear();
		this.roles.addAll(newRoles);
		ArrayList<String> rolesNameList = new ArrayList<String>();
		for (UserRole role : newRoles) {
			rolesNameList.add(role.getName());
		}
		replaceListField("role", rolesNameList);
	}
	
	public void setMembers(Set<String> newMembers){
		members.clear();
		members.addAll(newMembers);
		ArrayList<String> usersID = new ArrayList<String>();
		for (String user : newMembers){
			usersID.add(user);
		}
		replaceListField("members", usersID);
	}

	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		setName(getValueForDoc(saveFieldsMap, "groupname", fields).valueAsText);
		setDescription(getValueForDoc(saveFieldsMap, "description", fields).valueAsText);
		setDefaultRuleID(getValueForDoc(saveFieldsMap, "defaultruleid", fields).valueAsText);
		setOwner(getValueForDoc(saveFieldsMap, "ownergroup", fields).valueAsText);
		
		ArrayList<UserRole> newRoles = new ArrayList<UserRole>();
		
		if (fields.containsKey("role")) {
			for (String roleName: fields.get("role")) {
				Role role = env.globalSetting.roleCollection.getRolesMap().get(roleName);
				if (role != null){		
					role.setRuleProvider(UserRoleType.PROVIDED_BY_GROUP);
					newRoles.add(new UserRole(role));
				}			
			}
		}
		setRoles(newRoles);
		
		HashSet<String> newMembers = new HashSet<String>();
		if (fields.containsKey("members")){
			for (String userID : fields.get("members")){
				newMembers.add(userID);
			}			
		}
		setMembers(newMembers);
	}

	public int isRead(){		
		return 1;
	}

	@Override
    public int save(User user) throws LicenseException {
		UserGroup existGroup = struct.getGroup(docID, sysGroupAsSet, sysUser);
		int docID = 0;
		
		normalizeViewTexts();
		
		if (existGroup == null) {
			docID = struct.insertGroup(this);
			setDocID(docID);
			setNewDoc(false);
		} else {
			docID = struct.updateGroup(this);
		}

/*		for (String userID : this.getMembers()){
			Employer emp = struct.getAppUser(userID);
			if (emp != null && !(emp.hasGroup(this.getName()))) {
				emp.addGroup(this);
				emp.save(sysGroupAsSet, sysUser);
			}
		}*/

		return docID;
	}
	
	public String toString(){
		return this.getName() + " " + this.getDescription();
	}
	
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof UserGroup) {
			if (this.getDocID() == ((UserGroup)o).getDocID()) {
				return true;
			}
		}
		return false;
	}
}
