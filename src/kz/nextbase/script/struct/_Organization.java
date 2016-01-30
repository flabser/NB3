package kz.nextbase.script.struct;

import kz.flabs.dataengine.Const;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.nextbase.script._Document;
import kz.nextbase.script._Session;

public class _Organization  extends _Document implements Const{
	public Organization organization;

	public _Organization(Organization org,_Session ses) {
		super(org, ses);	
		this.organization = org;					
	}

	public boolean isNew(){
		return organization.isNewDoc();
	}

	public void setFullName(String fn) {
		organization.setFullName(fn);
	}
	
	public String getFullName() {
		return organization.getFullName();
	}

	public void setShortName(String fn) {
		organization.setShortName(fn);
	}
	
	public String getShortName() {
		return organization.getShortName();
	}
	
	public void setAddress(String fn) {
		organization.setAddress(fn);
	}
	
	public void setComment(String t) {
		organization.setComment(t);
	}

    public void setBIN(String value) {organization.setBIN(value);}

    public String getBIN() {return organization.getBIN();}

    public int isMain() {return organization.getIsMain();}

    public void setMain(int value) {organization.setIsMain(value);}
	
}
