package kz.flabs.runtimeobj.document.structure;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.flabs.webrule.form.ISaveField;

import java.util.Date;
import java.util.HashMap;

public class Organization extends Document {
	
	private static final long serialVersionUID = 1L;
	
	private String fullName;
	private String shortName;
	private String address;
	private String defaultServer;
	private String comment = "";
    private String bin;
	private int isMain;

	public Organization(IStructure struct){
		super(struct.getParent(), Const.sysUser);
		this.struct = struct;
		this.docType = Const.DOCTYPE_ORGANIZATION;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
		addStringField("address", address);
	}

	public String getDefaultServer() {
		return defaultServer;
	}

	public void setDefaultServer(String defaultServer) {
		this.defaultServer = defaultServer;
		addStringField("defaultserver", defaultServer);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		addStringField("comment", comment);
	}

	public int getIsMain() {
		return isMain;
	}

	public void setIsMain(int isMain) {
		this.isMain = isMain;
		addNumberField("isMain", isMain);
	}	

    public String getBIN() {
        return bin;
    }

    public void setBIN(String bin) {
        this.bin = bin;
        addStringField("bin", bin);
    }

	public int isRead(){		
		return 1;
	}
	
	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		setFullName(getValueForDoc(saveFieldsMap,"fullname", fields).valueAsText);		
		setShortName(getValueForDoc(saveFieldsMap,"shortname", fields).valueAsText);
		setAddress(getValueForDoc(saveFieldsMap,"address", fields).valueAsText);
		setComment(getValueForDoc(saveFieldsMap,"comment", fields).valueAsText);
		setDefaultServer(getValueForDoc(saveFieldsMap,"defaultserver", fields).valueAsText);
	}

    @Override
	public int save(User user) throws DocumentAccessException, DocumentException{
		Organization existOrg = struct.getOrganization(docID, user);
		int docID = 0;
        computeViewText();
        normalizeViewTexts();
		if (existOrg == null){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = struct.insertOrganization(this);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			if (getViewDate() == null) setViewDate(getRegDate());
			docID = struct.updateOrganization(this);
		}	
		return docID;
	}
	
	public String toString(){
		return fullName;
	}

	/*public String toXML(){		
		return "<fields><fullname>" + fullName + "</fullname><shortname>" + shortName + "</shortname>" +
		"<comment>" + comment + "</comment><address>" + address + "</address><ismain>" + isMain + "</ismain></fields>";
	}*/

}
