package kz.flabs.runtimeobj.document;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.ISaveField;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class Execution extends Document implements Const {
	public String executor;
	public String report;

	private static final long serialVersionUID = 1L;
	private Date finishDate;
	private int ndelo;
	private int nomentype;

    public String toXMLEntry(String value) {
        return "<entry hasattach=\"" + Integer.toString(0) + "\" doctype=\"" + docType + "\"  " +
                "docid=\"" + docID + "\"" + XMLUtil.getAsAttribute("viewtext", getViewText()) +
                "url=\"Provider?type=edit&amp;element=execution&amp;id=execution&amp;key=" + docID + "\" " +
                ">" + value + "</entry>";

    }

	public Execution(AppEnv env, User currentUser) {
		super(env, currentUser);
		docType = Const.DOCTYPE_EXECUTION;
		form = "execution";
	}

	public Execution(IDatabase db, String currentUser) {
		super(db, currentUser);
		docType = Const.DOCTYPE_EXECUTION;
		form = "execution";
	}

	public void setExecutor(String exec){

		executor = exec;
        addStringField("executor", exec);

	}

	public String getExecutor(){
		return executor;
	}

	public void setReport(String r){
		if (r.length() > 640){
			this.report = r.substring(0, 639);
		}else{
			this.report = r;
		}
		addStringField("report", this.report);
	}

	public String getReport(){
		return report;
	}

	public void setFinishDate(Date fd){
		this.finishDate = fd;
		addDateField("finishdate", fd);
	}

	public Date getFinishDate() throws DocumentException{
		if (finishDate == null){
			throw new DocumentException(DocumentExceptionType.DATE_VALUE_INCORRECT,"finishdate");
		}
		return finishDate;
	}

	public void setNomenType(int type){
		this.nomentype = type;
		addNumberField("nomentype", type);
	}

	public int getNomenType(){
		return this.nomentype;
	}

	public void setNdelo(int ndelo){
		this.ndelo = ndelo;
        addNumberField("ndelo", ndelo);
	}

	public int getNdelo(){
		return this.ndelo;
	}

	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		setExecutor(getValueForDoc(saveFieldsMap,"executor", fields).valueAsText);
		setReport(getValueForDoc(saveFieldsMap, "report", fields).valueAsText);
		setFinishDate(getValueForDoc(saveFieldsMap,"finishdate", fields).valueAsDate);
		setNomenType(getValueForDoc(saveFieldsMap, "nomentype", fields).valueAsNumber.intValue());
		setDefaultRuleID(getValueForDoc(saveFieldsMap, "defaultruleid", fields).valueAsText);

		for(ISaveField saveField: saveFieldsMap.values()){
			if (saveField.getSourceType() == ValueSourceType.WEBFORMFILE){
				blobDataProcess(saveField, fields);
			}
		}

	}



	public String getURL() {
		return "Provider?type=edit&element=execution&id=execution&key=" + docID;
	}

	public String getFile(String fieldName){
		//IDatabase dataBase = env.dataBase;
	//	filePath = dataBase.getMainDocumentAttach(Integer.parseInt(key), userSession.user.userID, fieldName, attachmentName);
		return "";

	}

	protected void setViewText() throws DocumentException {
		String vtext = "";
		Employer executor = db.getStructure().getAppUser(getExecutor());
		vtext += Util.dateTimeFormat.format(getFinishDate());
		vtext += " " + executor.getShortName();
		vtext += " (" + getReport() + ")";
		this.setViewText(vtext);
	}

	public int save(Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException, DocumentException{
		int docID = 0;
        setViewText();
        if (!normalizeViewTexts()){
			normalizeViewTexts();
		}
        User user = new User(absoluteUserID);
		if(isNewDoc()){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = db.getExecutions().insertExecution(this, user);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			docID = db.getExecutions().updateExecution(this, user);
		}
		return docID;
	}

}
