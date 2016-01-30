package kz.flabs.runtimeobj.document.project;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.ISaveField;

import java.util.*;

public class Project extends Document implements Const, ICoordConst {

    private static final long serialVersionUID = 1L;
    private HashMap<Integer, Block> blocks = new HashMap<Integer, Block>();
    private HashMap<String, Recipient> recipients = new HashMap<String, Recipient>();
    private int coordStatus = ICoordConst.STATUS_UNDEFINED;
    private Glossary projectGlossary;
    private Glossary contragentGlossary;
    private Employer techEngineer;
    private Glossary categoryGlossary;

    public String toXMLEntry(String value) {
        return "<entry hasattach=\"" + Integer.toString(0) + "\" doctype=\"" + docType + "\"  " +
                "docid=\"" + docID + "\"" + XMLUtil.getAsAttribute("viewtext", getViewText()) +
                "url=\"Provider?type=edit&amp;element=project&amp;id=" + form + "&amp;key=" + docID + "\" " +
                ">" + value + "</entry>";

    }

    public Project(AppEnv env, User currentUser) {
        super(env, currentUser);
        docType = Const.DOCTYPE_PROJECT;
    }

    public Project(IDatabase db, String currentUser) {
        super(db, currentUser);
        docType = Const.DOCTYPE_PROJECT;
    }

    public void setStatus(int s) {
        coordStatus = s;
        addNumberField("coordstatus", coordStatus);
    }

    public int getStatus() {
        return coordStatus;
    }

    public void setResponsiblePost(String post){
        addStringField("respost", post);
    }

    public String getResponsiblePost() throws DocumentException{
        return getValueAsString("respost")[0];
    }

    public void setControlDate(Date ctrlDate) {
        addDateField("ctrldate", ctrlDate);
    }

    public Date getControlDate() throws DocumentException{
        return getValueAsDate("ctrldate");
    }

    public void setSubcategory(int value) {
        addNumberField("subcategory", value);
    }

    public int getSubcategory() throws DocumentException{
        return getValueAsInteger("subcategory");
    }

    public String getAmountDamage() throws DocumentException {
        return getValueAsString("amountdamage")[0];
    }

    public void setAmountDamage(String amount) {
        addStringField("amountdamage", amount);
    }

    public int getCity() throws DocumentException {
        return getValueAsInteger("city");
    }

    public String getCoordinats() throws DocumentException {
        return getValueAsString("coordinats")[0];
    }

    public String getStreet() throws DocumentException {
        return getValueAsString("street")[0];
    }

    public String getContragent() throws DocumentException {
        return getValueAsString("contragent")[0];
    }

    public void setContragent(String value) {
        addStringField("contragent", value);
        if (value != null && !"".equalsIgnoreCase(value)) {
            try {
                int contragent = Integer.valueOf(value);
                setContragentGlossary(contragent);
            } catch (NumberFormatException nfe) {
                setContragentGlossary(0);
            }
        }
    }

    public String getPodryad() throws DocumentException {
        return getValueAsString("podryad")[0];
    }

    public void setPodryad(String value) {
        addStringField("podryad", value);
    }

    public String getSubpodryad() throws DocumentException {
        return getValueAsString("subpodryad")[0];
    }

    public void setSubpodryad(String value) {
        addStringField("subpodryad", value);
    }

    public String getExecutor() throws DocumentException {
        return getValueAsString("executor")[0];
    }

    public void setExecutor(String value) {
        addStringField("executor", value);
    }
    public String getHouse() throws DocumentException {
        return getValueAsString("house")[0];
    }

    public String getPorch() throws DocumentException {
        return getValueAsString("porch")[0];
    }

    public String getFloor() throws DocumentException {
        return getValueAsString("floor")[0];
    }

    public String getApartment() throws DocumentException {
        return getValueAsString("apartment")[0];
    }

    public void setCoordinats(String value) {
        addStringField("coordinats", value);
    }

    public void setCity(int value) {
        addNumberField("city", value);
    }

    public void setStreet(String value) {
        addStringField("street", value);
    }

    public void setHouse(String value) {
        addStringField("house", value);
    }

    public void setPorch(String value) {
        addStringField("porch", value);
    }

    public void setFloor(String value) {
        addStringField("floor", value);
    }

    public void setApartment(String value) {
        addStringField("apartment", value);
    }

    @Deprecated
    public int getHar() throws DocumentException{
        return 0;
    }

    @Deprecated
    public void setHar(int character){
        addNumberField("har", character);
    }

    public Glossary getContragentGlossary() {
        return this.contragentGlossary;
    }

    public void setContragentGlossary(int contragent){
        Glossary contragentDoc = db.getGlossaries().getGlossaryDocumentByID(contragent);
        this.contragentGlossary = contragentDoc;
    }

    public int getProject() throws DocumentException{
        return getValueAsInteger("project");
    }

    public Glossary getProjectGlossary() {
        return this.projectGlossary;
    }

    public void setProjectGlossary(int project) {
        Glossary projectDoc = db.getGlossaries().getGlossaryDocumentByID(project);
        this.projectGlossary = projectDoc;
    }

    public void setProject(int project){
        addNumberField("project", project);
        setProjectGlossary(project);
    }

    public void setTechEngineer(String userID){
        Employer engineer = db.getStructure().getAppUser(userID);
        this.techEngineer = engineer;
    }

    public Employer getTechEngineer() {
        return this.techEngineer;
    }

    public void setResponsibleSection(String userID) {
        addStringField("responsible", userID);
        setTechEngineer(userID);
    }

    public String getResponsibleSection() throws DocumentException {
        return getValueAsString("responsible")[0];
    }

    public void setCategory(int cat){
        addNumberField("category", cat);
        setCategoryGlossary(cat);
    }

    public void setCategoryGlossary(int cat) {
        Glossary category = db.getGlossaries().getGlossaryDocumentByID(cat);
        this.categoryGlossary = category;
    }

    public int getCategory() throws DocumentException{
        return getValueAsInteger("category");
    }

    public Glossary getCategoryGlossary() {
        return this.categoryGlossary;
    }

    public void setOrigin(String origin){
        addStringField("origin", origin);
    }

    public String getOrigin() throws DocumentException{
        return getValueAsString("origin")[0];
    }

    public void setProjectDate(Date dateRes){
        addDateField("projectdate", dateRes);
    }

    public Date getProjectDate() throws DocumentException{
        return getValueAsDate("projectdate");
    }

    public void setAutoSendAfterSign(int autoSendAfterSign) {
        addNumberField("autosendaftersign", autoSendAfterSign);
    }

    public int getAutoSendAfterSign() throws DocumentException {
        return getValueAsInteger("autosendaftersign");
    }

    public int getRegDocID() throws DocumentException {
        return getValueAsInteger("regdocid");
    }

    public void setRegDocID(int regdocid) {
        addNumberField("regdocid", regdocid);
    }

    public void setAutoSendToSign(int autoSendToSign){
        addNumberField("autosendtosign", autoSendToSign);
    }
    public int getAutoSendToSign() throws DocumentException {
        return getValueAsInteger("autosendtosign");
    }

    public void setBriefContent(String briefContent){
        addStringField("briefcontent", briefContent);
    }
    public String getBriefContent() throws DocumentException {
        return getValueAsString("briefcontent")[0];
    }

    public String getContentSource() {
        try {
            return Util.removeHTMLTags(getValueAsString("contentsource")[0]);
        } catch (DocumentException de) {
            return "";
        }
    }

    public void setCoorDBlockCount(int coorDBlockCount){
        addNumberField("coordblockcount", coorDBlockCount);
    }
    public int getCoorDBlockCount() throws DocumentException {
        return getValueAsInteger("coordblockcount");
    }

    public void setCoorDBlockLeft(int coorDBlockLeft){
        addNumberField("coordblockleft", coorDBlockLeft);
    }
    public int getCoorDBlockLeft() throws DocumentException {
        return getValueAsInteger("coordblockleft");
    }


    public void setCoordStatus(int coordStatus){
        addNumberField("coordstatus", coordStatus);
    }


    public int getCoordStatus() throws DocumentException {
        return getValueAsInteger("coordstatus");
    }

    public void setDocVersion(int docVersion){
        addNumberField("docversion", docVersion);
    }
    public int getDocVersion() throws DocumentException {
        return getValueAsInteger("docversion");
    }

    public void setIsRejected(int isRejected){
        addNumberField("isrejected", isRejected);
    }

    public int getIsRejected() throws DocumentException {
        return getValueAsInteger("isrejected");
    }

    public void addRecipient(Recipient rec){
        recipients.put(rec.getUserID(), rec);
    }

    public void addRecipient(String recipient){
        this.recipients.put(recipient, new Recipient(recipient));
    }

    public void setRecipients(ArrayList<String> recipients) {
    	clearRecipients();
        for (String recipient: recipients) {
            this.recipients.put(recipient, new Recipient(recipient));
        }
    }

    public void clearRecipients() {
        recipients.clear();
    }

    public void setRecipient(Collection<String> recipients){
        clearRecipients();
        for (String recipient : recipients) {
            addRecipient(recipient);
        }
    }

    public String getRecipient() throws DocumentException {
        return getValueAsString("recipient")[0];
    }

    public Collection<Recipient> getRecipients() throws DocumentException {
        return recipients.values();
    }

    public Set<String> getRecipientsList() throws DocumentException {
        return recipients.keySet();
    }

    public void setSender(String recipient){
        addStringField("sender", recipient);
    }
    
    public String getSender() throws DocumentException {
        return getValueAsString("sender")[0];
    }

    public void setSigner(String signer){
        for(Block bl: blocks.values()){
            if (bl.type == TO_SIGN){
                for(Coordinator coord: bl.getCoordinators()){
                    if (coord.type == COORDINATOR_TYPE_SIGNER){
                        coord.userID = signer;
                    }
                }
            }
        }

    }

    public Coordinator getSigner() throws DocumentException {
        for (Block bl: blocks.values()){
            if (bl.type == TO_SIGN){
                for(Coordinator coord: bl.getCoordinators()){
                    if(coord.type == COORDINATOR_TYPE_SIGNER){
                        return coord;
                    }
                }
            }
        }
        return null;
    }

    public Block getSignBlock() throws DocumentException {
        for(Block bl: blocks.values()){
            if (bl.type == TO_SIGN){
                return bl;
            }
        }
        return null;
    }


    public void setForm(String form){
        addStringField("form", form);
    }
    public String[] getForm() throws DocumentException {
        return getValueAsString("form");
    }


    public void setDocFolder(String DocFolder){
        addStringField("docfolder", DocFolder);
    }

    public void setContentSource(String text) {
        addStringField("contentsource", text);
    }

    public String getDocFolder() throws DocumentException {
        return getValueAsString("docfolder")[0];
    }


    public void setDeliveryType(String DeliveryType){
        addStringField("deliverytype", DeliveryType);
    }
    
    public String getDeliveryType() throws DocumentException {
        return getValueAsString("deliverytype")[0];
    }

    public int getNomenType() throws DocumentException{
        return getValueAsInteger("nomentype");
    }

    public void setNomenType(int nomen){
        addNumberField("nomentype", nomen);
    }

    public void setVn(String vn){
        addStringField("vn", vn);
    }
    public String getVn() throws DocumentException{
        return getValueAsString("vn")[0];
    }

    @Deprecated
    public void setVnNumber(int vnnum) {
        addNumberField("vnnumber", vnnum);

    }
    
    public int getVnNumber() throws DocumentException {
    	return getValueAsInteger("vnnumber");
    }

    @Deprecated
    public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException{
        String status = getValueForDoc(saveFieldsMap,"coordstatus", fields).valueAsText;

        if (status.equalsIgnoreCase("draft")){
            setStatus(ICoordConst.STATUS_DRAFT);
        }

        setProjectDate(getValueForDoc(saveFieldsMap,"projectdate", fields).valueAsDate);
        setAutoSendAfterSign(getValueForDoc(saveFieldsMap,"autosendaftersign", fields).valueAsNumber.intValue());
        setAutoSendToSign(getValueForDoc(saveFieldsMap,"autosendtosign", fields).valueAsNumber.intValue());
        setBriefContent(getValueForDoc(saveFieldsMap,"briefcontent", fields).valueAsText);
        setRecipient(getValueForDoc(saveFieldsMap,"recipient", fields).valuesAsStringList);
        setSender(getValueForDoc(saveFieldsMap,"sender", fields).valueAsText);
        setDocVersion(getValueForDoc(saveFieldsMap,"docversion", fields).valueAsNumber.intValue());
        setDeliveryType(getValueForDoc(saveFieldsMap,"deliverytype", fields).valueAsText);
        setRegDocID(getValueForDoc(saveFieldsMap, "regdocid", fields).valueAsNumber.intValue());
        setDocFolder(getValueForDoc(saveFieldsMap,"docfolder", fields).valueAsText);
        setForm(getValueForDoc(saveFieldsMap, "form", fields).valueAsText);
        setContentSource(getValueForDoc(saveFieldsMap, "contentsource", fields).valueAsText);
        setNomenType(getValueForDoc(saveFieldsMap, "nomentype", fields).valueAsNumber.intValue());
        setHar(getValueForDoc(saveFieldsMap, "har", fields).valueAsNumber.intValue());
        setProject(getValueForDoc(saveFieldsMap, "project", fields).valueAsNumber.intValue());
        setCategory(getValueForDoc(saveFieldsMap, "category", fields).valueAsNumber.intValue());
        setOrigin(getValueForDoc(saveFieldsMap, "origin", fields).valueAsText);
        setDefaultRuleID(getValueForDoc(saveFieldsMap, "defaultruleid", fields).valueAsText);
        setCoordinats(getValueForDoc(saveFieldsMap, "coordinats", fields).valueAsText);
        setCity(getValueForDoc(saveFieldsMap, "city", fields).valueAsNumber.intValue());
        setStreet(getValueForDoc(saveFieldsMap, "street", fields).valueAsText);
        setHouse(getValueForDoc(saveFieldsMap, "house", fields).valueAsText);
        setPorch(getValueForDoc(saveFieldsMap, "porch", fields).valueAsText);
        setFloor(getValueForDoc(saveFieldsMap, "floor", fields).valueAsText);
        setApartment(getValueForDoc(saveFieldsMap, "apartment", fields).valueAsText);
        setResponsibleSection(getValueForDoc(saveFieldsMap, "responsible", fields).valueAsText);
        setControlDate(getValueForDoc(saveFieldsMap, "ctrldate", fields).valueAsDate);
        setSubcategory(getValueForDoc(saveFieldsMap, "subcategory", fields).valueAsNumber.intValue());
        setContragent(getValueForDoc(saveFieldsMap, "contragent", fields).valueAsText);
        setPodryad(getValueForDoc(saveFieldsMap, "podryad", fields).valueAsText);
        setSubpodryad(getValueForDoc(saveFieldsMap, "subpodryad", fields).valueAsText);
        setExecutor(getValueForDoc(saveFieldsMap, "executor", fields).valueAsText);
        setResponsiblePost(getValueForDoc(saveFieldsMap, "respost", fields).valueAsText);
        setAmountDamage(getValueForDoc(saveFieldsMap, "amountdamage", fields).valueAsText);
        /** it is temporary code **/
        addStringField("action", getValueForDoc(saveFieldsMap,"action", fields).valueAsText);

        //blocks.clear();
        String blocks[] = fields.get("coordBlock");
        if (blocks != null){
            ProjectBlockParser blockParser = new ProjectBlockParser();
            ArrayList<Block> newBlocks = new ArrayList<Block>();
            for(String blockVal: blocks) {
                Block block = blockParser.parse(this, blockVal);
                newBlocks.add(block);
            }
            setBlocks(newBlocks);

        }
        
        for(ISaveField saveField: saveFieldsMap.values()){
            if (saveField.getSourceType() == ValueSourceType.WEBFORMFILE){
                blobDataProcess(saveField, fields);
            }
        }
        
    }

    public String getURL() throws DocumentException {
        return "Provider?type=edit&element=project&id=" + this.getForm()[0] + "&key=" + docID;
    }

    public Block getFirstBlock() {
        int minBlockNum = 0;
        Block block = null;
        while(block == null && minBlockNum <= blocks.size()){
            block = blocks.get(++minBlockNum);
        }
        if (block == null) {
            block = blocks.get(-1);
        }
        return block;
    }

    private Block formNewBlock(Block block, int newBlockNumber) {
        if (block.type == TO_SIGN) {
            block.setBlockNum(-1);
            return block;
        }
        if (block.isNew) {
            block.setBlockNum(newBlockNumber);
        }
        return block;
    }

    public void addBlock(Block block) {
        blocks.put(block.getBlockNumber(), block);
    }

    public void setBlocks(ArrayList<Block> blocks) {
        //ArrayList<Block> newBlocks = new ArrayList<Block>();
        HashMap<Integer, Block> newBlocks = new HashMap<Integer, Block>();
        for (Block block: blocks) {
            int numberForSearch = 0;
            if (block.getType() == TO_SIGN) {
                numberForSearch = -1;
            } else {
                numberForSearch = block.getBlockNumber();
            }
            if (this.blocks.containsKey(numberForSearch)) {
                Block existingBlock = this.blocks.get(numberForSearch);
                if (block.type != 0) existingBlock.type = block.type;
                existingBlock.setDelayTime(block.getDelayTime());
                if (!block.isFromWebForm) {
                    existingBlock.status = block.status;
                }
                if (existingBlock.getType() == TO_SIGN) {
                    existingBlock.setBlockNum(-1);
                }
                ArrayList<Coordinator> newCoordinators = new ArrayList<Coordinator>();
                ArrayList<Coordinator> existingCoordinators = new ArrayList<Coordinator>(existingBlock.getCoordinators());
                for (Coordinator coordinator: block.getCoordinators()) {
                    if (existingCoordinators.contains(coordinator)) {
                        Coordinator existingCoordinator = null;
                        for (int i = 0; i < existingCoordinators.size(); i++) {
                            if (existingCoordinators.get(i).equals(coordinator)) {
                                existingCoordinator = existingCoordinators.get(i);
                                break;
                            }
                        }
                        if (!coordinator.isFromWebForm) {
                            existingCoordinator.setCoordType(coordinator.getCoordType());
                            existingCoordinator.decision = coordinator.decision;
                            existingCoordinator.setComment(coordinator.getComment());
                            existingCoordinator.isCurrent(coordinator.isCurrent());
                            existingCoordinator.setCoorDate(coordinator.getCoorDate());
                            existingCoordinator.setDecisionDate(coordinator.getDecisionDate());
                        }
                        newCoordinators.add(existingCoordinator);
                    } else {
                        newCoordinators.add(coordinator);
                    }
                }
                existingBlock.coordinators.clear();
                for (Coordinator newCoordinator: newCoordinators) {
                    existingBlock.addCoordinator(newCoordinator);
                }
                newBlocks.put(existingBlock.getBlockNumber(), existingBlock);
            } else {
                Block newBlock = formNewBlock(block, getNextBlockNumber(newBlocks));
                newBlocks.put(newBlock.getBlockNumber(), newBlock);
            }
        }
        this.blocks.clear();
        this.blocks.putAll(newBlocks);
    }

    public Block getCurrentBlock() {
        for(Block block:blocks.values()){
            if(block.status == ICoordConst.BLOCK_STATUS_COORDINATING){
                return block;
            }
        }
        return null;
    }

    public Block getBlock(int num) {
        return blocks.get(num);
    }

    public Block getNextBlock(Block prevBlock) {
        Block block = null;
        if (prevBlock.getBlockNumber() == -1 || prevBlock.getType() == TO_SIGN) {
            return block;
        }
        int startBlockNum = prevBlock.getBlockNumber();
        while(block == null && startBlockNum <= blocks.size()){
            block = blocks.get(++startBlockNum);
        }
        if (block == null) {
            block = blocks.get(-1);
        }
        return block;
    }

    private int getNextBlockNumber(HashMap<Integer, Block> blocks) {
        int startBlockNum = 1;
        int foundBlocks = 0;
        int comparator = 0;
        if (blocks.containsKey(-1)) {
            comparator = blocks.size() - 1;
        } else {
            comparator = blocks.size();
        }
        while (blocks.containsKey(startBlockNum) || foundBlocks < comparator) {
            if (blocks.containsKey(startBlockNum)) {
                foundBlocks++;
            }
            startBlockNum++;
        }
        return startBlockNum;
    }

	/* 1 1, 2 2, 3 3, 4 3, 5 4, 6 4, 7 4, 8 4, 9 5, 10 stop
		1 ------
		2 ------
		9 ------
		5 ------
		3 ------
	 */

    public Block createBlock(){
        return new Block(this);
    }

    public Collection<Block> getBlocksList(){
        return blocks.values();
    }

    public HashMap<Integer, Block> getBlocksMap(){
        return blocks;
    }

    protected void setViewText() throws DocumentException {
        String vtext = "";
        vtext += this.getVnNumber() != 0 ? " * " + this.getVn() : "";
        vtext += " " + Util.dateTimeFormat.format(this.getProjectDate());
        Employer author = db.getStructure().getAppUser(getAuthorID());
        if (author == null){
            vtext += " " + getAuthorID() + " " + getBriefContent();
        }else{
            vtext += " " + author.getShortName() + " " + getBriefContent();
        }
        this.setViewText(vtext);
    }

    public int save(Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException, DocumentException{
        int docID = 0;
        normalizeViewTexts();
        User user = new User(absoluteUserID);
        if(isNewDoc()){
            setRegDate(new Date());
            setLastUpdate(getRegDate());
            docID = db.getProjects().insertProject(this, user);
            setDocID(docID);
            setNewDoc(false);
        }else{
            setLastUpdate(new Date());
            if (getViewDate() == null) setViewDate(getRegDate());
            docID = db.getProjects().updateProject(this, user);
        }
        return docID;
    }

    public String toString(){
        return "coordstatus=" + fieldsMap.get("coordstatus") + ", vn=" + fieldsMap.get("vn") + ", ddbid=" + ddbID;
    }

    public StringBuffer getRecipientsAsXML() throws DocumentException {
        StringBuffer xmlContent = new StringBuffer(10000);
        for (Recipient recipient : recipients.values()) {
            String userName = "", userID = recipient.getUserID();
            if (!userID.equals("")){
                if (this.form.equals("workdocprj")) {
                    IStructure struct = db.getStructure();
                    Employer emp = struct.getAppUser(recipient.getUserID());
                    if(emp != null){
                        userName = emp.getFullName();
                        userID = emp.getUserID();
                    }else{
                        AppEnv.logger.warningLogEntry("error 4354 \"" + recipient.getUserID() + "\"");
                    }

                } else {
                    int glossaryID = Integer.valueOf(userID);
                    Glossary g = db.getGlossaries().getGlossaryDocumentByID(glossaryID, true, Const.sysGroupAsSet, Const.sysUser);
                    userName = g.getViewText();
                }
                xmlContent.append("<entry>");
                xmlContent.append("<user attrval=\"" + userID + "\" >" + userName + "</user>");
                xmlContent.append("</entry>");
            }
        }
        return xmlContent;
    }

    @Deprecated
    public StringBuffer getBlocksAsXML() throws DocumentException {
        StringBuffer xmlContent = new StringBuffer(10000);
        for(Block bl: blocks.values()){
            xmlContent.append("<entry num=\"" + bl.getBlockNumber() + "\" type=\"" + bl.type + "\" >");
            if (bl.type != TO_SIGN){
                xmlContent.append("<status>" + bl.status + "</status>");
                xmlContent.append("<delaytime>" + bl.delayTime + "</delaytime>");
                xmlContent.append("<coordinators>");
                for(Coordinator coord: bl.getCoordinators()){
                    if (coord.type == COORDINATOR_TYPE_REVIEWER){
                        xmlContent.append(fillCoordPiece(coord));
                    }
                }
                xmlContent.append("</coordinators>");
            }else{
                xmlContent.append("<status>" + bl.status + "</status>");
                xmlContent.append("<delaytime>" + bl.delayTime + "</delaytime>");
                xmlContent.append("<signers>");
                for(Coordinator coord: bl.getCoordinators()){
                    if (coord.type == COORDINATOR_TYPE_SIGNER){
                        xmlContent.append(fillCoordPiece(coord));
                    }
                }
                xmlContent.append("</signers>");
            }
            xmlContent.append("</entry>");
        }
        return xmlContent;
    }

   @Deprecated 
    private StringBuffer fillCoordPiece(Coordinator coord) throws DocumentException{
        StringBuffer xmlContent = new StringBuffer(500);
        String userName = "", userID = "";
        IStructure struct = db.getStructure();
        Employer emp = struct.getAppUser(coord.userID);
        if(emp != null){
            userName = emp.getFullName();
            userID = emp.getUserID();
        }else{
            AppEnv.logger.warningLogEntry("Employer for \"" + coord.userID + "\" has not found");
        }
        xmlContent.append("<entry  num=\"" + coord.num + "\">");
        xmlContent.append("<user attrval=\"" + userID + "\" >" + userName + "</user>");
        xmlContent.append("<iscurrent>" + coord.isCurrent() + "</iscurrent>");
        xmlContent.append("<decision>" + coord.getDecis() + "</decision>");
        xmlContent.append("<decisiondate>" + (coord.getDecisionDate() != null ? Util.dateTimeFormat.format(coord.getDecisionDate()) : "") + "</decisiondate>");
        xmlContent.append("<comment>" + coord.getComment() + "</comment>");
        xmlContent.append("</entry>");
        return xmlContent;
    }

}