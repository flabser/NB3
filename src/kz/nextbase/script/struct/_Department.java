package kz.nextbase.script.struct;

import kz.flabs.dataengine.Const;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.nextbase.script._Document;
import kz.nextbase.script._Session;

public class _Department  extends _Document implements Const{
	public Department department;

	public _Department(Department dep,_Session ses) {
		super(dep, ses);	
		this.department = dep;					
	}

	public boolean isNew(){
		return department.isNewDoc();
	}
	
	public void setParent(String[] cKey, String parentID, String parentType) {
		department.setParent(cKey, parentID, parentType);
	}

	public void setFullName(String fn) {
		department.setFullName(fn);
	}
	
	public String getFullName() {
		return department.getFullName();
	}
	
	public void setShortName(String fn) {
		department.setShortName(fn);
	}

	public String getShortName() {
		return department.getShortName();
	}
	
	public void setComment(String t) {
		department.setComment(t);
	}
	
	public String getComment() {
		return department.getComment();
	}
	
	public void setRank(int rank){
		department.setRank(rank);
	}
	
	public int getRank() {
		return department.getRank();
	}
	
	public void setIndex(String index){
		department.setIndex(index);
	}
	
	public String getIndex() {
		return department.getIndex();
	}
	
	public void setType(int t){
		department.setType(t);
	}
	
	public int getType() {
		return department.getType();
	}
	
}
