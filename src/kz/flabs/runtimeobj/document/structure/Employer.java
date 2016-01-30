package kz.flabs.runtimeobj.document.structure;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.users.User;
import kz.flabs.util.adapters.UserAdapter;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.form.ISaveField;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

@XmlAccessorType(XmlAccessType.NONE)
public class Employer extends Document implements Cloneable{
    public static final long serialVersionUID = 1L;

    protected String currentUser;
    @XmlElement(name = "fullname")
	private String fullName = "";
    private String shortName = "";
    private String comment = "";
    private String index = "";
    private String phone = "";
    private String skin = "";
	private int hits;
	private int countdocinview;
	private int depID;
	private int orgID;
	private int bossID;
	private int isBoss;
	private int postID;
	private int rank;
	private int sendTo;
	private int obl;
	private int region;
	private int village;
    private int status;
    private Date birthDate;

    @XmlElement(name = "userid")
    @XmlJavaTypeAdapter(UserAdapter.class)
    private User user;

    
	private HashSet<UserRole> roles = new HashSet<UserRole>();

    
    private HashSet<UserGroup> groups = new HashSet<UserGroup>();

    
    private HashSet<Filter> filters = new HashSet<Filter>();

	
	public Employer(IStructure struct) {
		super(struct.getParent(), Const.sysUser);
		this.struct = struct;
		user = new User(struct.getParent().getParent());
		docType = DOCTYPE_EMPLOYER;
	}

    @Deprecated
    public Employer() {
        super(new AppEnv(""), new User("anonymous"));
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        this.addNumberField("status", status);
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

	public void setUser(User user){
		this.user = user;
	}

	public User getUser(){
		return user;
	}

	public boolean hasRole(UserRole role) {
		return getAllUserRoles().contains(role.getName());
	}


	public boolean hasRole(String roleName) {
		return getAllUserRoles().contains(roleName);
		
	}
	
	public String getAllGroups() {
		return groups.toString();
	}

	public boolean hasGroup(String groupName) {
		for (UserGroup group : groups) {
			if (group.getName().equalsIgnoreCase(groupName)) {
				return true;
			}
		}
		return false;
	}	

	public boolean hasGroup(int groupID) {
		for (UserGroup group : groups) {
			if (group.getDocID() == groupID) {
				return true;
			}
		}
		return false;
	}

	public void setReplacers(HashMap<String, String[]> fields){
		if(fields.containsKey("replacer")) {
			String groupName = "[" + this.getUserID() + "]";
            User sysUser = new User(Const.sysUser);
			UserGroup replaceGroup = this.struct.getGroup(groupName, Const.sysGroupAsSet, Const.sysUser);
			if (replaceGroup == null) {
				replaceGroup = new UserGroup(this.struct);
				replaceGroup.setName(groupName);
				replaceGroup.setViewText(groupName);
				replaceGroup.setDescription("");
			} else {
				replaceGroup.resetMembers();
			}
			String[] replacers = fields.get("replacer");
			for (String replacerID : replacers) {
				replaceGroup.addMember(replacerID);
			}
			replaceGroup.setOwner(this.getUserID());
			try {
				replaceGroup.save(sysUser);
			} catch (LicenseException e) {
				DatabaseUtil.errorPrint(e);
				return;
			}
		}
	}

	public void addRole(UserRole role) {	
		if (roles.add(role)){
			addValueToList("role", role.getName());
		}
	}

	public StringBuffer getFiltersAsXML() {
		StringBuffer xmlContent = new StringBuffer(10000);
		for (Filter filter : filters) {
			xmlContent.append("<entry name=\"" + filter.getName() + "\" mode=\"" + filter.getEnable() + "\" filterid=\"" + filter.getFilterID() + "\">");
			HashMap<String, String> conds = filter.getConditions();
			for (String conName : conds.keySet()) {
				if (conName.equalsIgnoreCase("author")) {
					Employer author = db.getStructure().getAppUser(conds.get(conName));
					if (author != null) {
						xmlContent.append("<" + conName + " userid=\"" + author.getUserID() + "\">" + author.getFullName() + "</" + conName + ">");	
					} else {
						xmlContent.append("<" + conName + ">" + conds.get(conName) + "</" + conName + ">");
					}
				} else {
					xmlContent.append("<" + conName + ">" + conds.get(conName) + "</" + conName + ">");
				}
			}
			xmlContent.append("</entry>");
		}				
		return xmlContent;		
	}

	public void setFilters(HashSet<Filter> filters) {
		this.filters = filters;
	}

	public void addGroup(UserGroup group) {
		if (!groups.contains(group)) {
			groups.add(group);
			addValueToList("group", Integer.toString(group.getDocID()));
		}
	}

	public void addFilter(Filter filter) {
		if (!filters.contains(filter)) {
			filters.add(filter);
			addValueToList("filter", filter.getName());
		}
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

	public void setGroups(ArrayList<UserGroup> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
		ArrayList<String> groupsID = new ArrayList<String>();
		for (UserGroup group : groups) {
			groupsID.add(Integer.toString(group.getDocID()));
		}
		this.replaceListField("group", groupsID);
	}

	public HashSet<UserRole> getRoles() {
		return roles;
	}

	public HashSet<UserGroup> getGroups() {
		return groups;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
		addField("fullname",fullName, FieldType.TEXT);		
	}

	public String getShortName() {
		return shortName;
	}
	public int getCountDocInView() {
		return countdocinview;
	}
	public String getSkin() {
		return skin;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
		addField("shortname",shortName, FieldType.TEXT);	
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
		addField("index", index, FieldType.TEXT);
	}

	public String getPhone() {
		return phone;
	}

    public String getPublicKey() {
        return this.user.getPublicKey();
    }

	public void setPhone(String phone) {
		this.phone = phone;
		addField("phone", phone, FieldType.TEXT);
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
        addNumberField("rank", rank);
	}

	public void setSkin(String skin) {
		this.skin = skin;
		addField("skin", skin,  FieldType.TEXT);
	}
	public void setCountDocInView(int countdocinview) {
		this.countdocinview = countdocinview;
        addNumberField("countdocinview", countdocinview);
	}
	public void setObl(int value) {
		this.obl = value;
        addNumberField("obl", value);
	}

	public void setRegion(int value) {
		this.region = value;
        addNumberField("region", value);
	}

	public void setVillage(int value) {
		this.village = value;
        addNumberField("village", value);
	}

	public int getObl() {
		return obl;
	}

	public int getRegion() {
		return region;
	}

	public int getVillage() {
		return village;
	}

	public int getSendto() {
		return sendTo;
	}

	public void setSendTo(int sendto) {
		this.sendTo = sendto;
        addNumberField("sendto", sendto);
	}

	public String getUserID() {
		return user.getUserID();
	}


	public void setBoss(int isBoss) {
		this.isBoss = isBoss;
        addNumberField("isboss", isBoss);
	}

	public boolean isBoss() {
		return isBoss > 0;
	}

	public void setUserID(String userID) {
		addField("userid",userID, FieldType.TEXT);
		user.setUserID(userID);
	}

	public void setUniqueUserID(String userID) throws StructException {
		if (isNewDoc()){
			ISystemDatabase sysDB = DatabaseFactory.getSysDatabase();			
			User u = sysDB.getUser(userID);
			if (u.isValid){			
				user = u;			
			}else{
				setUserID(userID);	
				//throw new StructException(StructExceptionType.NOT_UNIQUE_USERNAME);		
			}
		}else{
			if (!userID.equalsIgnoreCase(user.getUserID())){
				throw new StructException(StructExceptionType.USERNAME_DOES_NOT_CHANGE);		
			}
		}
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		addField("comment",comment, FieldType.TEXT);
	}

	public String getPost() throws DocumentException {
		Glossary post = this.db.getStructure().getGlossaryDocumentByID(postID);
		if (post != null){
			return post.getName();
		}else{
			return "";
		}
	}


	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
		addField("hits",hits);
	}

	public int getDepID() {
		return depID;
	}

	public int getOrgID() {
		return orgID;
	}

	public int getBossID() {
		return bossID;
	}

	public void setDepID(int depID) {
		this.depID = depID;
		addField("depid",depID);
	}

	public void setOrgID(int orgID) {
		this.orgID = orgID;
		addField("orgid", orgID);
	}

	public void setBossID(int bossID) {
		this.bossID = bossID;
		addField("bossid", bossID);
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public Department getDepartment(){
		User user = new User(sysUser);
        if (this.getDepID() != 0){
			return struct.getDepartment(depID, user);
		}else{
			return null;
		}	
	}

	public void setNotesAddress(String na){

	}

	public void setPostID(int post) {
		this.postID = post;
		addField("postid", post);
	}

	public int getPostID() {
		return this.postID;
	}

	public int isRead(){		
		return 1;
	}

	@Deprecated
	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException {
		String userID = getValueForDoc(saveFieldsMap,"userid", fields).valueAsText;
		user.setUserID(userID);
		setFullName(getValueForDoc(saveFieldsMap,"fullname", fields).valueAsText);		
		setShortName(getValueForDoc(saveFieldsMap,"shortname", fields).valueAsText);
		setComment(getValueForDoc(saveFieldsMap,"comment", fields).valueAsText);
		setIndex(getValueForDoc(saveFieldsMap, "index", fields).valueAsText);
		setRank(getValueForDoc(saveFieldsMap, "rank", fields).valueAsNumber.intValue());
		setPhone(getValueForDoc(saveFieldsMap, "phone", fields).valueAsText);
		setPostID(getValueForDoc(saveFieldsMap, "postid", fields).valueAsNumber.intValue());
		setSendTo(getValueForDoc(saveFieldsMap, "sendto", fields).valueAsNumber.intValue());
		setObl(getValueForDoc(saveFieldsMap, "obl", fields).valueAsNumber.intValue());
		setRegion(getValueForDoc(saveFieldsMap, "region", fields).valueAsNumber.intValue());
		setVillage(getValueForDoc(saveFieldsMap, "village", fields).valueAsNumber.intValue());
        setBirthDate(getValueForDoc(saveFieldsMap, "birthdate", fields).valueAsDate);

		ArrayList<UserRole> newRoles = new ArrayList<UserRole>();
		ArrayList<UserGroup> newGroups = new ArrayList<UserGroup>();
		if (fields.containsKey("role")) {
			for (String roleName: fields.get("role")) {
				Role role = env.globalSetting.roleCollection.getRolesMap().get(roleName);
				if (role != null){					
					newRoles.add(new UserRole(role));
				}			
			}
		}
		if (fields.containsKey("group")) {
			for (String groupID : fields.get("group")) {
				newGroups.add(struct.getGroup(Integer.valueOf(groupID), sysGroupAsSet, sysUser));
			}
		}

		User sysUser;
		if (this.user == null){
			sysUser = DatabaseFactory.getSysDatabase().getUser(userID);
		}else{
			sysUser = this.user;
		}
		if (fields.containsKey("password")) {			
			sysUser.setPassword(fields.get("password")[0]);
		}
		//if (fields.containsKey("password")) {			
		//	sysUser.setPasswordHash(fields.get("password")[0]);
		//}

		String app[] = fields.get("enabledapps");
		String lm[] = fields.get("loginmode");
		sysUser.enabledApps.clear();
		if (app != null){
			for(int i = 0; i < app.length; i++){
				if (!app[i].equals("")){
					int loginMode = 0;
					try{
						loginMode = Integer.parseInt(lm[i]);
					}catch(NumberFormatException e){
						loginMode = 0;
					}
					UserApplicationProfile ap = new UserApplicationProfile(app[i],loginMode);
					if (loginMode == 1){
						String[] q = fields.get("question_" + ap.appName);
						String[] a = fields.get("answer_" + ap.appName);
						for(int i1 = 0; i1 < q.length; i1 ++){
							UserApplicationProfile.QuestionAnswer qa = ap.new QuestionAnswer(q[i1].trim(), a[i1].trim());
							ap.getQuestionAnswer().add(qa);
						}
					}
					sysUser.addEnabledApp(ap.appName, ap);
				}
			}
		}


		if (fields.containsKey("email")) {
			String[] email = fields.get("email");
			if (email != null) {
				sysUser.setEmail(email[0]);
			}			
		}



		try{
			String depID = fields.get("depid")[0];
			if (!depID.equalsIgnoreCase("")) {
				this.parentDocID = Integer.parseInt(depID);
				this.parentDocType = Const.DOCTYPE_DEPARTMENT;
			} else {
				this.parentDocID = Integer.parseInt(fields.get("parentdocid")[0]);
				this.parentDocType = Integer.parseInt(fields.get("parentdoctype")[0]);
			}

		}catch(NumberFormatException e){
			this.parentDocID = 0;
			this.parentDocType = Const.DOCTYPE_UNKNOWN;
		}
		this.setRoles(newRoles);
		this.setGroups(newGroups);
		this.setUser(sysUser);
		switch(this.parentDocType){
		case DOCTYPE_ORGANIZATION:
			setOrgID(parentDocID);
			break;
		case DOCTYPE_DEPARTMENT:
			setDepID(parentDocID);
			break;
		case DOCTYPE_EMPLOYER:
			setBossID(parentDocID);
			break;
		}

	}

	public HashSet<Filter> getFilters() {
		return filters;
	}

	public String getAllFilters() {
		return filters.toString();
	}

	public int save(User user) throws LicenseException{
		Employer existEmpl = struct.getEmployer(docID, user);
		int docID;
        normalizeViewTexts();
        if (this.user.isValid){
            DatabaseFactory.getSysDatabase().update(this.user);
        }else{
            DatabaseFactory.getSysDatabase().insert(this.user);
        }
        if (existEmpl == null){
            setRegDate(new Date());
            setLastUpdate(getRegDate());
            docID = struct.insertEmployer(this);
            setDocID(docID);
            setNewDoc(false);
        }else{
            setLastUpdate(new Date());
            if (getViewDate() == null) setViewDate(getRegDate());
            docID = struct.updateEmployer(this);
        }
        return docID;
	}


	public HashSet<String> getAllUserRoles(){
		HashSet<String> userRoles = new HashSet<String>();
		for (UserRole role : this.getRoles()) {
			userRoles.add(role.getName());
		}


		for (UserGroup group : groups) {
			for (UserRole role : group.getRoles()) {
				userRoles.add(role.getName());
			}
		}

		return userRoles;
	}

	public HashSet<UserRole> getAllRoles(){
		HashSet<UserRole> userRoles = new HashSet<UserRole>();
		for (UserRole role : this.getRoles()) {
			userRoles.add(role);
		}


		for (UserGroup group : groups) {
			for (UserRole role : group.getRoles()) {
				userRoles.add(role);
			}
		}

		return userRoles;
	}
	
	public HashSet<String> getAllUserGroups(){
		return initAllUserGroups();
	}

/*	public String getFormattedUserGroups() {
		String groups = "'" + getUserID() + "'";
		for (String group : this.getAllUserGroups()) {
			groups += ", '" + group + "'";
		}
		return groups;
	}*/

	public String toString(){
		return " docID=" + docID + ", docType=" + docType + ", depID=" + depID + ", fullName=" + fullName;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} 
		if (o instanceof Employer) {
			if (this.getDocID() == ((Employer)o).getDocID()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public Employer clone() {
		try {	
			return (Employer)super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw new InternalError();
		}
	}
	
	private HashSet<String> initAllUserGroups(){
		HashSet<String> userGroups = new HashSet<String>();

		for (UserGroup	group : this.getGroups()) {
			userGroups.add(group.getName());
		}

		Department dep = this.getDepartment();
		if (dep != null) {
			for (UserGroup group : dep.getGroups()) {
				userGroups.add(group.getName());
			}
		}
		userGroups.add(this.getUserID());
		return userGroups;
	}


}
