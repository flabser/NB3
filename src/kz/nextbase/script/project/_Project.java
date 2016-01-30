package kz.nextbase.script.project;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.project.Block;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;
import kz.nextbase.script._Document;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._Session;
import kz.nextbase.script.constants._CoordStatusType;

import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;

@Deprecated
public class _Project extends _Document implements ICoordConst {
	private Project project;

	public _Project(Project document, String user) {
		super(document, user);	
		this.project = document;
	}

	public _Project(Project document, _Session ses){
		super(document, ses);		
		this.project = document;
	}

	public String getContragent() throws DocumentException {
		return this.project.getContragent();
	}

	public int getCategory() throws DocumentException {
		return this.project.getCategory();
	}

	public int getProject() throws DocumentException {
		return this.project.getProject();
	}

	public _Project(Project document) {
		super(document);
		this.project = document;
	}

	public String getResponsibleSection() throws DocumentException {
		return project.getResponsibleSection();
	}

	public void setResponsibleSection(String userID) {
		project.setResponsibleSection(userID);
	}

	public void setSubcategory(int value) {
		project.setSubcategory(value);
	}
	public void setContragent(String value) {
		project.setContragent(value);
	}
	public void setPodryad(String value) {
		project.setPodryad(value);
	}

	public void setSubpodryad(String value) {
		project.setSubpodryad(value);
	}

	public void setExecutor(String value) {
		project.setExecutor(value);
	}

	public void setAmountDamage(String amount) {
		project.setAmountDamage(amount);

	}
	public void setProject(int prj){
		project.setProject(prj);
	}
	public void setCategory(int cat){
		project.setCategory(cat);	

	}
	public void setNomenType(int nomen){
		project.setNomenType(nomen);
	}


	public void setAutoSendAfterSign(int autoSendAfterSign) {
		project.addNumberField("autosendaftersign", autoSendAfterSign);
	}

	public int getAutoSendAfterSign() throws DocumentException {
		return project.getValueAsInteger("autosendaftersign");
	}

	public Date getCtrlDate() throws DocumentException {
		return project.getControlDate();
	}

	public void setCtrlDate(Date ctrlDate) {
		project.setControlDate(ctrlDate);
	}

	public void setContentSource(String text) {
		project.setContentSource(text);
	}
	public void setAutoSendToSign(int autoSendToSign){
		super.addNumberField("autosendtosign", autoSendToSign);
	}
	public int getAutoSendToSign() throws DocumentException, _Exception {
		return super.getValueNumber("autosendtosign");
	}


	public void setBriefContent(String briefContent) throws _Exception{
		super.addStringField("briefcontent", briefContent);
	}
	public String getBriefContent() throws DocumentException {
		return super.getValueString("briefcontent");
	}

	public void setRegDocID(int regdocid) {
		project.setRegDocID(regdocid);
	}

	@Deprecated
	public void  setHar(int har){
		project.setHar(har);
	}

	public void setOrigin(String origin){
		project.setOrigin(origin);
	}

	public void setCoordinats(String cor){
		project.setCoordinats(cor);
	}

	public void  setCity(int city){
		project.setCity(city);
	}

	public void  setStreet(String street){
		project.setStreet(street);
	}

	public void setHouse(String value) {
		project.setHouse(value);
	}

	public void setPorch(String value) {
		project.setPorch(value);
	}

	public void setFloor(String value) {
		project.setFloor( value);
	}

	public void setApartment(String value) {
		project.setApartment(value);
	}

	public void setResponsiblePost(String post){
		project.setResponsiblePost(post);
	}


	public int getRegDocID() throws DocumentException {
		return project.getRegDocID();
	}

	public void setCoorDBlockCount(int coorDBlockCount){
		super.addNumberField("coordblockcount", coorDBlockCount);
	}
	public int getCoorDBlockCount() throws _Exception {
		return super.getValueNumber("coordblockcount");
	}


	public void setCoorDBlockLeft(int coorDBlockLeft){
		super.addNumberField("coordblockleft", coorDBlockLeft);
	}
	public int getCoorDBlockLeft() throws _Exception {
		return super.getValueNumber("coordblockleft");
	}

	public void sendToSignining() throws DocumentException{
		project.setCoordStatus(ICoordConst.STATUS_SIGNING);
		project.addReader(project.getSigner().userID);
	}


	
	
	public void setCoordStatus(_CoordStatusType status){
		if (status == _CoordStatusType.DRAFT){
			project.setCoordStatus(ICoordConst.STATUS_DRAFT);
		}else if(status == _CoordStatusType.COORDINATING){
			project.setCoordStatus(ICoordConst.STATUS_COORDINTING);
		}else if(status == _CoordStatusType.COORDINATED){
			project.setCoordStatus(ICoordConst.STATUS_COORDINTED);
		}else if(status == _CoordStatusType.REJECTED){
			project.setCoordStatus(ICoordConst.STATUS_REJECTED);
		}else if(status == _CoordStatusType.SIGNED){
			project.setCoordStatus(ICoordConst.STATUS_SIGNED);
		}else if(status == _CoordStatusType.SIGNING){
			project.setCoordStatus(ICoordConst.STATUS_SIGNING);
		}else if(status == _CoordStatusType.NOCOORDINATION){
			project.setCoordStatus(ICoordConst.STATUS_NOCOORDINATION);		
		}else if(status == _CoordStatusType.EXPIRED){
			project.setCoordStatus(ICoordConst.STATUS_EXPIRED);		
		}else if(status == _CoordStatusType.NEWVERSION){
			project.setCoordStatus(ICoordConst.STATUS_NEWVERSION);	
		}else if (status == _CoordStatusType.EXECUTING) {
			project.setCoordStatus(ICoordConst.STATUS_EXECUTING);
		}else if (status == _CoordStatusType.EXECUTED) {
			project.setCoordStatus(ICoordConst.STATUS_EXECUTED);
		}else{
			project.setCoordStatus(ICoordConst.STATUS_UNDEFINED);
		}
	}

	@Deprecated
	public void setCoordStatus(String status){
		if (status.equalsIgnoreCase("draft")){
			project.setCoordStatus(ICoordConst.STATUS_DRAFT);
		}else if(status.equalsIgnoreCase("coordinating")){
			project.setCoordStatus(ICoordConst.STATUS_COORDINTING);
		}else if(status.equalsIgnoreCase("coordinated")){
			project.setCoordStatus(ICoordConst.STATUS_COORDINTED);
		}else if(status.equalsIgnoreCase("rejected")){
			project.setCoordStatus(ICoordConst.STATUS_REJECTED);
		}else if(status.equalsIgnoreCase("signed")){
			project.setCoordStatus(ICoordConst.STATUS_SIGNED);
		}else if(status.equalsIgnoreCase("signing")){
			project.setCoordStatus(ICoordConst.STATUS_SIGNING);
		}else if(status.equalsIgnoreCase("nocoordination")){
			project.setCoordStatus(ICoordConst.STATUS_NOCOORDINATION);		
		}else if(status.equalsIgnoreCase("expired")){
			project.setCoordStatus(ICoordConst.STATUS_EXPIRED);		
		}else if(status.equalsIgnoreCase("newversion")){
			project.setCoordStatus(ICoordConst.STATUS_NEWVERSION);	
		}else if (status.equalsIgnoreCase("executing")) {
			project.setCoordStatus(ICoordConst.STATUS_EXECUTING);
		}else if (status.equalsIgnoreCase("executed")) {
			project.setCoordStatus(ICoordConst.STATUS_EXECUTED);
		}else{
			project.setCoordStatus(ICoordConst.STATUS_UNDEFINED);
		}
	}
	
	@Deprecated
	public String getCoordStat() throws DocumentException {
		switch(project.getCoordStatus()){
		case ICoordConst.STATUS_DRAFT:
			return "draft";
		case ICoordConst.STATUS_COORDINTING:
			return "coordinating";
		case ICoordConst.STATUS_COORDINTED:
			return "coordinated";
		case ICoordConst.STATUS_REJECTED:
			return "rejected";
		case ICoordConst.STATUS_SIGNED:
			return "signed";
		case ICoordConst.STATUS_SIGNING:
			return "signing";
		case ICoordConst.STATUS_NEWVERSION:
			return "newversion";
		case ICoordConst.STATUS_NOCOORDINATION:
			return "nocoordination";
		case ICoordConst.STATUS_EXPIRED:
			return "expired";
		case ICoordConst.STATUS_EXECUTING:
			return "executing";
		case ICoordConst.STATUS_EXECUTED:
			return "executed";
		default:
			return "undefined";
		}

	}

	
	public _CoordStatusType getCoordStatus() throws DocumentException {
		switch(project.getCoordStatus()){
		case ICoordConst.STATUS_DRAFT:
			return _CoordStatusType.DRAFT;
		case ICoordConst.STATUS_COORDINTING:
			return _CoordStatusType.COORDINATING;
		case ICoordConst.STATUS_COORDINTED:
			return _CoordStatusType.COORDINATED;
		case ICoordConst.STATUS_REJECTED:
			return _CoordStatusType.REJECTED;
		case ICoordConst.STATUS_SIGNED:
			return _CoordStatusType.SIGNED;
		case ICoordConst.STATUS_SIGNING:
			return _CoordStatusType.SIGNING;
		case ICoordConst.STATUS_NEWVERSION:
			return _CoordStatusType.NEWVERSION;
		case ICoordConst.STATUS_NOCOORDINATION:
			return _CoordStatusType.NOCOORDINATION;
		case ICoordConst.STATUS_EXPIRED:
			return _CoordStatusType.EXPIRED;
		case ICoordConst.STATUS_EXECUTING:
			return _CoordStatusType.EXECUTING;
		case ICoordConst.STATUS_EXECUTED:
			return _CoordStatusType.EXECUTED;
		default:
			return _CoordStatusType.UNDEFINED;
		}

	}

	@Deprecated
	public int getCoordStatusInt() throws _Exception{
		try {
			return project.getCoordStatus();
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getCoordStatusInt()");
		}
	}

	public void setDocVersion(int docVersion){
		super.addNumberField("docversion", docVersion);
	}
	public int getDocVersion() throws  _Exception {
		return super.getValueNumber("docversion");
	}


	public void setIsRejected(int isRejected){
		super.addNumberField("isrejected", isRejected);
	}

	public int getIsRejected() throws  _Exception {
		return super.getValueNumber("isrejected");
	}

	public void addRecipient(String recipient){
		project.addRecipient(recipient);
	}
	
	public void setRecipients(String[] recipients) {
		project.clearRecipients();
        for (String recipient: recipients) {
        	project.addRecipient(recipient);
        }		
	}
	
	public Collection<String> getRecipients() throws DocumentException {
		return project.getRecipientsList();
	}

	public void setBlocks(ArrayList<_Block> blocks) {
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for (_Block block: blocks) {
			newBlocks.add(block.getBaseObject());
		}
		project.setBlocks(newBlocks);
	}

	public void setSender(String recipient) throws _Exception{
		super.addStringField("sender", recipient);
	}

	public String getSender() {
		return super.getValueString("sender");
	}


	public void setSigner(String signer){
		project.setSigner(signer);
	}

	public _Coordinator getSigner()throws DocumentException {
		Coordinator coord = project.getSigner();
		if (coord != null){
			return new _Coordinator(coord);
		}
		return null;
	}


	public void setCurrentRecipient(String currentRecipient) throws _Exception{
		super.addStringField("currentrecipient", currentRecipient);
	}


	public _Block getCurrentBlock() throws DocumentException {
		Block b = project.getCurrentBlock();
		if (b != null){
			return new _Block(b);
		}
		return null;
	}

	public _Block getFirstBlock() throws DocumentException {
		Block b = project.getFirstBlock();
		if (b != null){
			return new _Block(b);
		}
		return null;
	}

	public _Block getSignBlock() throws DocumentException {
		Block b = project.getSignBlock();
		if (b != null){
			return new _Block(b);
		}
		return null;
	}

	public int getNomenType() throws DocumentException{
		return project.getNomenType();
	}

	public _Block getNextBlock(_Block prevBlock) {
		Block b = project.getNextBlock(prevBlock.getBaseObject());
		if (b != null){
			return new _Block(b);
		}
		return null;
	}

	public ArrayList<_Block> getBlocks(){
		ArrayList<_Block> blocks = new ArrayList<_Block>();
		for (Block block: project.getBlocksList()){
			blocks.add(new _Block(block));
		}
		return blocks;
	}

	public ArrayList<_Block> getCoordBlocks(){
		ArrayList<_Block> blocks = new ArrayList<_Block>();
		for (Block block: project.getBlocksList()){
			if (block.getType() == PARALLEL_COORDINATION || block.getType() == SERIAL_COORDINATION){
				blocks.add(new _Block(block));
			}
		}
		return blocks;
	}

	public void setDocFolder(String DocFolder) throws _Exception{
		super.addStringField("docfolder", DocFolder);
	}
	public String getDocFolder() throws DocumentException {
		return super.getValueString("docfolder");
	}


	public void setDeliveryType(String DeliveryType) throws _Exception{
		super.addStringField("deliverytype", DeliveryType);
	}
	public String getDeliveryType() throws DocumentException {
		return super.getValueString("deliverytype");
	}


	public void setVn(String vn) throws _Exception{
		addStringField("vn", vn);
	}
	
	public String getVn() throws DocumentException{
		return super.getValueString("vn");
	}
	
	public int getVnNumber() throws DocumentException, _Exception {
		return super.getValueNumber("vnnumber");
	}

	public void addBlock(_Block block){		
		project.addBlock(block.getBaseObject());
	}

	public Date getProjectDate() throws DocumentException{
		return project.getProjectDate();
	}

	public boolean save(){
		try {
			User user = new User(currentUserID);
			int result = project.save(user.getAllUserGroups(), currentUserID); 
			if (result > -1){
				return true;
			}else{
				return false;	
			}		
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentAccessException " + e.getMessage() + ", function: _Project.save(), returned:false");
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentException " + e.getMessage() + ", function: _Project.save(), returned:false");
		}
		return false;
	}

	public boolean save(String userID){
		try {
			User user = new User(userID);
			int result = project.save(user.getAllUserGroups(), userID); 
			if (result > -1){
				return true;
			}else{
				return false;	
			}			
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentAccessException " + e.getMessage() + ", function: _Project.save(" + userID + "), returned:false");
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry("DocumentException " + e.getMessage() + ", function: _Project.save(" + userID + "), returned:false");
		}
		return false;
	}

	public String getURL() {	
		
			return getURL();
	
	}

	public String getInfo(){
		String info = "";
		for(Block bl: project.getBlocksList()){
			info += " Block: " + bl.toString() + "\n";
			for(Coordinator coord: bl.getCoordinators()){
				info += "  Coordinator: " + coord + "\n";
			}
		}
		return "Project: " + project.toString() + "\n" + info;
	}

	public void setLastUpdate(Date dt){
		project.setLastUpdate(dt);
	}

	public void setProjectDate(Date dt){
		project.setProjectDate(dt);
	}

	public Project getBaseObject(){
		return project;
	}


}