package kz.flabs.runtimeobj.document.task;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.*;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.ISaveField;

import java.util.*;

public class Task extends Document implements Const {

	private static final long serialVersionUID = 1L;	
	private ArrayList<Executor> executors = new ArrayList<>();
	private String taskAuthor;
	private Employer authorRes;
	private String taskVn = "";
	private Date taskDate;
	private int DBD;
	private String content;
	private String briefContent;
	private String comment;
	private TaskType resolType;
	private Control control;
	private int har;
	private int project;
	private int category;
	private int apps;
	private int customer;

    public String toXMLEntry(String value) {
        return "<entry hasattach=\"" + Integer.toString(0) + "\" doctype=\"" + docType + "\"  " +
                "docid=\"" + docID + "\"" + XMLUtil.getAsAttribute("viewtext", getViewText()) +
                "url=\"Provider?type=edit&amp;element=task&amp;id=" + form + "&amp;key=" + docID + "\" " +
                ">" + value + "</entry>";

    }

	public Task(AppEnv env, User currentUser) {
		super(env, currentUser);
		docType = Const.DOCTYPE_TASK;
		addStringField("form", "task");
        addNumberField("doctype", docType);
		form = "task";	
	}

	public Task(IDatabase db, String currentUser) {
		super(db, currentUser);
		docType = Const.DOCTYPE_TASK;
		addStringField("form", "task");
		form = "task";
	}

	public void setTaskAuthor(String authorRes){		
		this.taskAuthor = authorRes;
		addStringField("taskauthor", authorRes);
		this.authorRes = db.getStructure().getAppUser(authorRes);
	}

	public Employer getAuthorRes(){
		return this.authorRes;
	}

	public String getTaskAuthor(){
		return taskAuthor;
	}

	public int getApps() {
		return this.apps;
	}

	public int getCustomer() {
		return this.customer;
	}

	public void setApps(int value) {
		this.apps = value;
        addNumberField("apps", value);
	}

	public void setCustomer(int value) {
		this.customer = value;
        addNumberField("customer", value);
	}

	public void setBriefContent(String content) {
		this.briefContent = content;
		addStringField("briefcontent", content);
	}

	public String getBriefContent() {
		return this.briefContent;
	}

	public void setHar(int character){
		this.har = character;
        addNumberField("har", character);
	}

	public int getHar(){
		return this.har;
	}

	public void setProject(int prj){
		this.project = prj;
        addNumberField("project", prj);
	}

	public void setCategory(int cat){
		this.category = cat;
        addNumberField("category", cat);
	}

	public int getCategory(){
		return category;
	}

	public int getProject(){
		return this.project;
	}

	public String getTaskVn() {
		return taskVn;
	}

	public void setTaskVn(String taskVn) {
		this.taskVn = taskVn;
		addStringField("taskvn", taskVn);
	}

	public void setTaskDate(Date dateRes){	
		this.taskDate = dateRes;
		addDateField("taskdate", dateRes);
	}

	public Date getTaskDate() throws DocumentException{
		if (taskDate == null){
			throw new DocumentException(DocumentExceptionType.DATE_VALUE_INCORRECT,"taskdate");
		}
		return taskDate;
	}

	public void setDBD(int dbd){	
		DBD = dbd;
        addNumberField("dbd", dbd);
	}

	public int getDBD() throws DocumentException{
		return DBD;
	}

	public void setContent(String content){	
		if (content.length() > 640){
			this.content = content.substring(0, 639);
		}else{
			this.content = content;
		}
		addStringField("content", this.content);
	}

	public String getContent(){
		return content;
	}

	public void setComment(String comment){		
		this.comment = comment;
	}

	public String getComment(){
		return comment;
	}

	public ArrayList<Executor> getExecutors(){
		return executors;
	}

	public Executor getExecutor(String execID){
		for (Executor exec : executors){
			if (exec.getID().equalsIgnoreCase(execID)){
				return exec;
			}
		}
		return null;
	}

	public void setResolType(TaskType resol){		
		resolType = resol;
		addBoolField("tasktype", resolType);
	}

	public TaskType getResolType(){
		return resolType;
	}

	public void setResolType(int resol){	
		if (resol == 1){
			resolType = TaskType.RESOLUTION;
		}else if (resol == 2){
			resolType = TaskType.CONSIGN;
		}else if (resol == 3){
			resolType = TaskType.TASK;
		}
		addBoolField("tasktype", resolType);
	}

	public int getResolTypeAsInt(){
		if (resolType == TaskType.RESOLUTION){
			return 1;
		}else if (resolType == TaskType.CONSIGN){
			return 2;
		}else if (resolType == TaskType.TASK){
			return 3;
		}
		return 0;
	}

	public Collection<Executor> getExecutorsList(){
		return executors;
	}

	@Deprecated
	protected void setViewText() throws DocumentException, ComplexObjectException{
		try {
			StringBuffer vtext = new StringBuffer(1000);
			

			vtext.append(this.getTaskVn().trim().length() == 0 ? "" : "*" + this.getTaskVn() + ": " );
			Date taskDate = this.getTaskDate() != null ? this.getTaskDate() : this.getRegDate();
			vtext.append(Util.dateTimeFormat.format(taskDate));
			Employer taskauthor = this.db.getStructure().getAppUser(this.getTaskAuthor());
			vtext.append(":" + taskauthor.getShortName() + " --> (");
			if (this.form.equalsIgnoreCase("kr")) {
				Collection<Executor> executors = this.getExecutorsList();
				String shortExecutorsNames = "";
				for(Executor e : executors){
					Employer emp = db.getStructure().getAppUser(e.getID());
					if (emp == null){
						shortExecutorsNames += e.getID() + ",";
					}else{
						shortExecutorsNames += emp.getShortName() + ",";
					}
				}
				if (shortExecutorsNames.length()>0)
					shortExecutorsNames = shortExecutorsNames.substring(0, shortExecutorsNames.length()-1);
				vtext.append(shortExecutorsNames + "), ");

				if (this.getResolType() == TaskType.TASK) {
					vtext.append(this.getBriefContent()); 
				} else {
					BaseDocument pdoc = new RuntimeObjUtil().getGrandParentDocument(db, this);
					if (pdoc.docType == Const.DOCTYPE_TASK && ((Task)pdoc).getResolType() == TaskType.TASK) {
						vtext.append(this.getBriefContent()); 
					} else {
						vtext.append(Util.removeHTMLTags(this.getContent())); 
					}
				}		
				Control ctrl = getControl();
				Date ctrlDate = ctrl.getExecDate();
				if (ctrlDate != null) {
					vtext.append(", " + Util.dateTimeFormat.format(ctrlDate));
				} 
			} else {
				String content = this.getContent();
				if (content != null) {
					content = Util.removeHTMLTags(content);
					if (content.length() > 256) {
						vtext.append(content, 0, 255);
					} else {
						vtext.append(content);
					}
				}
				vtext.append(")");
			}		
			this.setViewText(vtext.toString());
		} catch (DocumentAccessException e) {
			AppEnv.logger.errorLogEntry(e.getMessage());
		}
	}

	public StringBuffer getExecutorsAsXML() throws DocumentException{
		StringBuffer xmlContent = new StringBuffer(10000);
		IStructure struct = db.getStructure();		
		for(Executor exec: executors){
			String userName = "", userID = "";
			Employer emp = struct.getAppUser(exec.getID()); //!!
			if(emp != null){						
				userName = emp.getFullName();
				userID = emp.getUserID();				
			}else{
				AppEnv.logger.warningLogEntry("error 45483 \"" + exec.getID() + "\"");
			}
			xmlContent.append("<entry  num=\"" + exec.num + "\">");
			xmlContent.append("<user attrval=\"" + userID + "\" >" + userName + "</user>");
			xmlContent.append("<responsible>" + exec.getResponsible() + "</responsible>");
			xmlContent.append("<resetdate>" + (exec.getResetDate()!= null ? Util.convertDataTimeToString(exec.getResetDate()) : "") + "</resetdate>");
			xmlContent.append("<execpercent>" + exec.getPercentOfExecution() + "</execpercent>");

			String resetAuthor = "", resetAuthorID = "";
			if (!exec.resetAuthorID.equals("")){
				emp = struct.getAppUser(exec.resetAuthorID); //!!
				if(emp != null){							
					resetAuthor = emp.getFullName();
					resetAuthorID = emp.getUserID();				
				}else{
					AppEnv.logger.warningLogEntry("error 08756 \"" + exec.getID() + "\"");
				}
			}
			xmlContent.append("<resetauthor attrval=\"" + resetAuthorID + "\">" + resetAuthor + "</resetauthor>");
			xmlContent.append("<comment>" + exec.comment + "</comment>");
			xmlContent.append("</entry>");
		}		
		return xmlContent;
	}

	

	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
		String executorsBlocks[] = fields.get("executor");
		if (executorsBlocks != null){
			clearAllExecutors();

			TaskExecutorParser parser = new TaskExecutorParser();
			for(String blockVal: fields.get("executor")){
				Executor executor = parser.parse(this, blockVal);
				if (executor.getID() != null){
					addExecutor(executor);	
				}
			}

		}
		setTaskAuthor(getValueForDoc(saveFieldsMap,"taskauthor", fields).valueAsText);	
		setTaskVn(getValueForDoc(saveFieldsMap,"taskvn", fields).valueAsText);
		setTaskDate(getValueForDoc(saveFieldsMap,"taskdate", fields).valueAsDate);
		setContent(getValueForDoc(saveFieldsMap,"content", fields).valueAsText);
		setComment(getValueForDoc(saveFieldsMap,"comment", fields).valueAsText);
		setHar(getValueForDoc(saveFieldsMap, "har", fields).valueAsNumber.intValue());
		setProject(getValueForDoc(saveFieldsMap, "project", fields).valueAsNumber.intValue());
		setCategory(getValueForDoc(saveFieldsMap, "category", fields).valueAsNumber.intValue());
		setBriefContent(getValueForDoc(saveFieldsMap, "briefcontent", fields).valueAsText);
		setDefaultRuleID(getValueForDoc(saveFieldsMap, "defaultruleid", fields).valueAsText);
		setForm(getValueForDoc(saveFieldsMap, "form", fields).valueAsText);
		setApps(getValueForDoc(saveFieldsMap, "apps", fields).valueAsNumber.intValue());
		setCustomer(getValueForDoc(saveFieldsMap, "customer", fields).valueAsNumber.intValue());

		Control ctrl = new Control();
	//	ctrl.setType(getValueForDoc(saveFieldsMap, "controltype", fields).valueAsNumber);
		ctrl.setPrimaryCtrlDate(getValueForDoc(saveFieldsMap, "ctrldate", fields).valueAsDate);
		ctrl.setCycle(getValueForDoc(saveFieldsMap, "cyclecontrol", fields).valueAsNumber.intValue());
		ctrl.setAllControl(getValueForDoc(saveFieldsMap, "allcontrol", fields).valueAsNumber.intValue());
		ctrl.setOld(getValueForDoc(saveFieldsMap, "isold", fields).valueAsNumber.intValue());
		this.setControl(ctrl);

		for(ISaveField saveField: saveFieldsMap.values()){
			if (saveField.getSourceType() == ValueSourceType.WEBFORMFILE){
				blobDataProcess(saveField, fields);	
			}
		}

	}

	public void addExecutor(Executor executor) {
	//	executor.setParent(this);
		executors.add(executor);	
	}

	public void setControl(Control ctrl) {
		ctrl.setDocument(this);
		this.control = ctrl;
	}

	public Control getControl() {
		return this.control;
	}

	public void clearAllExecutors() {
		executors.clear();	
	}

	public String getURL() throws DocumentException {	
		return "Provider?type=edit&element=task&id=task&key=" + docID;
	}

	public int save( Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException, DocumentException{
		if (!normalizeViewTexts()){
			computeViewText();
			normalizeViewTexts();
		}
		int docID = 0;
		DBD =     (int) computeDiffBetweenDates(getControl().getExecDate());
        User user = new User(absoluteUserID);
		if(isNewDoc()){
			setRegDate(new Date());
			setLastUpdate(getRegDate());
			docID = db.getTasks().insertTask(this, user);
			setDocID(docID);
			setNewDoc(false);
		}else{
			setLastUpdate(new Date());
			if (getViewDate() == null) setViewDate(getRegDate());
			docID = db.getTasks().updateTask(this, user);
		}

		return docID;
	}

	public static long computeDiffBetweenDates(Date ctrlDate){
		long time1 = ctrlDate.getTime();
		long time2 = new Date().getTime();
		long diff = time1 - time2;
		return diff/Util.dayInMs;		
	}

}
