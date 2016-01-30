package kz.flabs.runtimeobj.document.structure;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.parser.ComplexKeyParser;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.flabs.webrule.form.ISaveField;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Department extends Document {

	private static final long serialVersionUID = 1L;

	private String fullName;
	private String shortName;
	private String comment = "";
	private String index;
	private int orgID;
	private int empID;
	private int mainID;
	private int hits;	
	private int rank;
	private int type;

	private ArrayList<UserGroup> groups = new ArrayList<UserGroup>();

	public Department(IStructure struct){
		super(struct.getParent(), Const.sysUser);
		this.struct = struct;
		this.docType = Const.DOCTYPE_DEPARTMENT;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
		addStringField("fullname", fullName);
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
		addStringField("shortname", shortName);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
		addNumberField("type", type);
	}	
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		addStringField("comment", comment);
	}

	public void setIndex(String index){
		this.index = index;
		addStringField("index", index);
	}

	public String getIndex(){
		return index;
	}

	public void setRank(int rank){
		this.rank = rank;
        addNumberField("rank", rank);
	}

	public int getRank(){
		return rank;
	}

	public int getOrgID() {
		return orgID;
	}

	public int getEmpID() {
		return empID;
	}

	public int getMainID() {
		return mainID;
	}

	public void setOrgID(int orgId) {
		this.orgID = orgId;
        addNumberField("orgid", orgId);
	}

	public void setEmpID(int empID) {
		this.empID = empID;
        addNumberField("empid", empID);
	}

	public void setMainID(int mainID) {
		this.mainID = mainID;
        addNumberField("mainid", mainID);
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
        addNumberField("hits", hits);
	}

	public int isRead(){		
		return 1;
	}

	@Deprecated
	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		setFullName(getValueForDoc(saveFieldsMap,"fullname", fields).valueAsText);		
		setShortName(getValueForDoc(saveFieldsMap,"shortname", fields).valueAsText);	
		setComment(getValueForDoc(saveFieldsMap,"comment", fields).valueAsText);
		setIndex(getValueForDoc(saveFieldsMap, "index", fields).valueAsText);
		setRank(getValueForDoc(saveFieldsMap, "rank", fields).valueAsNumber.intValue());
		setType(getValueForDoc(saveFieldsMap, "type", fields).valueAsNumber.intValue());
		ArrayList<UserGroup> newGroups = new ArrayList<UserGroup>();
		if (fields.containsKey("group")) {
			for (String groupID : fields.get("group")) {
				newGroups.add(struct.getGroup(Integer.valueOf(groupID), sysGroupAsSet, sysUser));
			}

		}
		this.setGroups(newGroups);
		
		setParent(fields.get("parentsubkey"), fields.get("parentdocid")[0], fields.get("parentdoctype")[0]);
			
	}

	public void setParent(String[] cKey, String parentID, String parentType){
		try{			
			if (cKey != null){
				String complexKey = cKey[0];
				if (!complexKey.equalsIgnoreCase("")) {
					List<DocID> key = ComplexKeyParser.parse(complexKey);				
					this.parentDocID = key.get(0).id;
					this.parentDocType = key.get(0).type;
				} else {
					this.parentDocID = Integer.parseInt(parentID);
					this.parentDocType = Integer.parseInt(parentType);
				}
			}else{
				this.parentDocID = Integer.parseInt(parentID);
				this.parentDocType = Integer.parseInt(parentType);
			}
		}catch(NumberFormatException e){
			this.parentDocID = 0;
			this.parentDocType = Const.DOCTYPE_UNKNOWN;
		}
		
		resetStructIDS();

		switch(this.parentDocType){
		case DOCTYPE_ORGANIZATION:
			setOrgID(parentDocID);
			break;
		case DOCTYPE_DEPARTMENT:
			setMainID(parentDocID);
			break;
		case DOCTYPE_EMPLOYER:
			setEmpID(parentDocID);
			break;
		}
	}

	public void resetStructIDS() {
		setOrgID(0);
		setMainID(0);
		setEmpID(0);
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

	public void deleteGroup(UserGroup group) {
		groups.remove(group);
	}

	public ArrayList<UserGroup> getGroups() {
		return groups;
	}

	public boolean hasGroup(int groupID) {
		for (UserGroup group : groups) {
			if (group.getDocID() == groupID) {
				return true;
			}
		}
		return false;
	}

	public boolean hasGroup(String groupName) {
		for (UserGroup group : groups) {
			if (group.getName().equalsIgnoreCase(groupName)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasGroup(UserGroup group) {
		return groups.contains(group);
	}

	public void addGroup(UserGroup group) {
		if (!groups.contains(group)) {
			groups.add(group);
			addValueToList("group", Integer.toString(group.getDocID()));
		}
	}
	
	public int save(User user){
		Department exist = struct.getDepartment(docID, user);
		int docID = 0;
        computeViewText();
        normalizeViewTexts();
		if (exist == null){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = struct.insertDepartment(this);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			if (getViewDate() == null) setViewDate(getRegDate());
			docID = struct.updateDepartment(this);
		}
		return docID;
	}

	public String toString(){
		return "orgID=" + orgID + ", fullName=" + fullName;
	}



}
