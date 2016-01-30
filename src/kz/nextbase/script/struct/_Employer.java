package kz.nextbase.script.struct;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.StructException;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.runtimeobj.document.structure.UserRole;
import kz.flabs.users.User;
import kz.flabs.webrule.Role;
import kz.nextbase.script.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class _Employer  extends _Document implements _IXMLContent, Const{
	public Employer employer;
	private String userID;

	public _Employer(Employer emp) {
		super(emp);	
		this.employer = emp;
		this.userID = emp.getUserID();				
	}

	public _Employer(Employer emp,_Session ses) {
		super(emp, ses);	
		this.employer = emp;
		this.userID = emp.getUserID();		
	}

	public boolean isNew(){
		return employer.isNewDoc();
	}

	public String getEmail() {
		ISystemDatabase sdb = DatabaseFactory.getSysDatabase();
		User user = sdb.getUser(userID);
		String email = user.getEmail();
		return ( email!= null ? email : "");
	}

	public String getInstMessengerAddr(){
		ISystemDatabase sdb = DatabaseFactory.getSysDatabase();
		User user = sdb.getUser(userID);
		String jid = user.getInstMsgAddress();
		return (jid != null ? jid : "");
	}

	public _Organization getOrganization() throws _Exception{
		try {
			return (_Organization)getGrandParentDocument();
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "userId=" + userID);
		} catch (DocumentAccessException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "userId=" + userID);
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, "userId=" + userID);
		}
	}

	public _Document getMainDepartment() throws _Exception{
		try {
			return (_Document)getParentDocument();
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "userId=" + userID);
		} catch (DocumentAccessException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, "userId=" + userID);
		}
	}

	public void setShortName(String fn) {
		employer.setShortName(fn);
	}

	public String getShortName() {
		return (employer != null ? employer.getShortName() : "");
	}
	public void setCountDocInView(int fn) {
		employer.setCountDocInView(fn);
	}

	public String getSkin() {
		return employer.getSkin();
	}

	public void setSkin(String fn) {
		employer.setSkin(fn);
	}

	public int getCountDocInView() {
		return employer.getCountDocInView();
	}
	public void setFullName(String fn) {
		employer.setFullName(fn);
	}

	public String getFullName() {
		return employer.getFullName();
	}

	public void setUserID(String id) throws _Exception {
		try{
			employer.setUniqueUserID(id);
		}catch (StructException e){
			throw new _Exception(_ExceptionType.USERNAME_DOES_NOT_CHANGE, "userId=" + userID, getSession());
		}
	}

	public String getUserID() {
		return userID;
	}

	public String getDepartment(){
		return (employer != null && employer.getDepartment() != null ? employer.getDepartment().getFullName() : "");
	}

	public int getDepartmentID() {
		return employer.getDepID();
	}

	public String getPost() throws _Exception{
		try{
			return employer.getPost();
		}catch(Exception e){
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Employer.getPost()");
		}
	}

	public void setPostID(String t) {
		int r = 999;
		try{
			r = Integer.parseInt(t);
		}catch(NumberFormatException e){

		}
		employer.setPostID(r);
	}

	public int getPostID() {
		return employer.getPostID();
	}

	public void setStatus(_EmployerStatusType st){
		switch (st){
		case HIRED: 
			employer.setStatus(0);
			break;
		case FIRED: 
			employer.setStatus(-1);
			break;
		}
	}

	public _EmployerStatusType getStatus(){
		int s = employer.getStatus();
		switch(s){
		case -1:
			return _EmployerStatusType.FIRED;
		case 0:
			return _EmployerStatusType.HIRED;
		default:
			return _EmployerStatusType.UNKNOWN;
		}
	}

	public Date getBirthDate() {
		return employer.getBirthDate();
	}

	public void setBirthDate(Date birthDate) {
		employer.setBirthDate(birthDate);
	}

	public int getRank() {
		return employer.getRank();
	}

	public String getPublicKey() {
		return employer.getPublicKey();
	}

	public String getPhone() {
		return employer.getPhone();
	}

	public int getSendto() {
		return employer.getSendto();
	}

	public String getComment() {
		return employer.getComment();
	}

	public HashSet<_UserRole> getListOfRoles() {
		HashSet<_UserRole> roles = new HashSet<_UserRole>();
		HashSet<UserRole> r = employer.getAllRoles();
		for(UserRole role: r){
			roles.add(new _UserRole(role));
		}
		return roles;
	}

	public HashSet<String> getListOfGroups() {
		return employer.getAllUserGroups();
	}

	public int getObl(){
		return employer.getObl();		
	}

	public int getRegion(){
		return employer.getRegion();
	}

	public int getVillage(){
		return employer.getVillage();		
	}

	public void setComment(String t) {
		employer.setComment(t);
	}

	public void setIndex(String t) {
		employer.setIndex(t);
	}

	public void setRank(String t) {
		int r = 999;
		try{
			r = Integer.parseInt(t);
		}catch(NumberFormatException e){

		}

		employer.setRank(r);
	}

	public void setPhone(String t) {
		employer.setPhone(t);
	}

	public void setSendTo(String t) {
		int r = 999;
		try{
			r = Integer.parseInt(t);
		}catch(NumberFormatException e){

		}

		employer.setSendTo(r);
	}

	public void setObl(String t) {
		int r = 999;
		try{
			r = Integer.parseInt(t);
		}catch(NumberFormatException e){

		}

		employer.setObl(r);
	}

	public void setRegion(String t) {
		int r = 999;
		try{
			r = Integer.parseInt(t);
		}catch(NumberFormatException e){

		}

		employer.setRegion(r);
	}

	public void setVillage(String t) {
		int r = 999;
		try{
			r = Integer.parseInt(t);
		}catch(NumberFormatException e){

		}

		employer.setVillage(r);
	}

	public void setListOfRoles(String[] t) {
		ArrayList<UserRole> newRoles = new ArrayList<UserRole>();
		if (!t[0].equals("")) {
			for (String roleName: t) {
				HashMap<String, Role> rolesMap = employer.env.getRolesMap();
				Role role = rolesMap.get(roleName);
				if (role != null){					
					newRoles.add(new UserRole(role));
				}			
			}
		}
		employer.setRoles(newRoles);
	}

	public void setListOfGroups(String[] t) {
		ArrayList<UserGroup> newGroups = new ArrayList<UserGroup>();
		if (!t[0].equals("")) {
			for (String groupID : t) {
				newGroups.add(employer.struct.getGroup(Integer.valueOf(groupID), sysGroupAsSet, sysUser));
			}
		}
		employer.setGroups(newGroups);
	}

	public void setPassword(String t) throws _Exception {
		if (t != null) {
			try{
				employer.getUser().setPassword(t);
			}catch (WebFormValueException e){
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "field=password");
			}
		}

	}

	public void setPasswordHash(String t) throws _Exception {
		if (t != "") {
			try{
				employer.getUser().setPasswordHash(t);
			}catch (WebFormValueException e){
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "field=password");
			}
		}

	}

	public void setEmail(String t) throws _Exception{
		if (t != null) {			
			try {
				employer.getUser().setEmail(t);
			}catch (WebFormValueException e){
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "field=email");
			}
		}
	}

	public void setMessenger(String t) throws _Exception{
		if (t != null) {			
			try {
				employer.getUser().setInstMsgAddress(t);
			}catch (WebFormValueException e){
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "field=instmsgaddress");
			}
		}
	}

	public void clearEnabledAppsList(){
		User user = employer.getUser();
		user.enabledApps.clear();

	}

	public void addEnabledApp(_UserApplicationProfile p) {
		UserApplicationProfile uap = p.profile;
		employer.getUser().addEnabledApp(uap.appName, uap);
	}

	public Collection<UserApplicationProfile> getEnabledApps() throws DocumentException {
		/*	String ea = "";
		for(UserApplicationProfile app: employer.getUser().enabledApps.values()){
			ea += "<entry>" + app.toXML() + "</entry>";		
		}
		return new SimpleIXMLContentWrapper(ea);*/
		User user = employer.getUser();

		return user.enabledApps.values();
	}

	public void setDepID(String t) throws _Exception{
		try{			
			if (!t.equalsIgnoreCase("")) {
				employer.parentDocID = Integer.parseInt(t);
				employer.parentDocType = Const.DOCTYPE_DEPARTMENT;
			}
		}catch(NumberFormatException e){

		}

		switch(employer.parentDocType){
		case DOCTYPE_ORGANIZATION:
			employer.setOrgID(employer.parentDocID);
			break;
		case DOCTYPE_DEPARTMENT:
			employer.setDepID(employer.parentDocID);
			break;
		case DOCTYPE_EMPLOYER:
			employer.setBossID(employer.parentDocID);
			break;
		}
	}

	public boolean hasPublicKey() {
		String pk = this.getPublicKey();
		return (pk != null && !pk.isEmpty() && !"null".equalsIgnoreCase(pk));
	}

	public boolean hasRole(String roleName) {
		return employer.hasRole(roleName);
	}

    public boolean hasGroup(String groupName) {
        return employer.hasGroup(groupName);
    }

	public boolean hasRole(List<String> rolesName) {
		try {		
			for (String r:rolesName){
				if (hasRole(r)){
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toXML() throws _Exception {
		String result = "<employer><userid>" +  employer.getUserID() + "</userid><fullname>" + employer.getFullName() + "</fullname></employer>";
		return result;
	}

	public boolean isAuthorized() {
		return employer.getUser().authorized;
	}

}
