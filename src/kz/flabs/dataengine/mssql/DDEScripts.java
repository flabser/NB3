package kz.flabs.dataengine.mssql;

import kz.flabs.dataengine.DatabaseUtil;

public class DDEScripts {

	public static String getMainDocsDDE(){
		String createString = "CREATE TABLE MAINDOCS(" +
				" DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_MAINDOCS PRIMARY KEY, " +
				" AUTHOR nvarchar(32)," +
				" PARENTDOCID int," +
				" PARENTDOCTYPE int," +
				" REGDATE datetime," +
				" DOCTYPE int, " +
				" LASTUPDATE datetime, " +
				" VIEWTEXT nvarchar(2048), " +
				" VIEWICON nvarchar(16), " +
				" FORM nvarchar(32), " +
				" SYNCSTATUS int, " +
				" HAS_ATTACHMENT int, " +
				" HAS_RESPONSE int DEFAULT 0, " +
                " HAS_TOPIC boolean DEFAULT false, " +
				" DEFAULTRULEID nvarchar(32), " +
				" DEL int, " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime, " +
				" SIGN nvarchar(4000)," +
				" TOPICID INT, " +
				" DDBID nvarchar(16)," +
                " PARENTDOCDDBID nvarchar(16)," +
                " APPID nvarchar(16), " +
				" SIGNEDFIELDS nvarchar(1200))";
		return createString;
	}

	public static String getSyncDDE() {
		String createString = "CREATE TABLE SYNC( " +
				" regdate datetime, " +
				" nexttime datetime, " +
				" cutoftime datetime, " +
				" [trigger] int, " +
				" doccount int, " +
				" type int, " +
				" errorcount int, " +
				" description nvarchar(1024))";
		return createString;	
	}

	public static String getGlossaryTreePath() {
		String createString = "CREATE TABLE GLOSSARY_TREE_PATH (" +
				" ANCESTOR INT NOT NULL, " +
				" DESCENDANT INT NOT NULL, " +
				" LENGTH int, " +
				" PRIMARY KEY(ANCESTOR, DESCENDANT), " +
				" FOREIGN KEY (ANCESTOR) REFERENCES GLOSSARY(DOCID), " +
				" FOREIGN KEY (DESCENDANT) REFERENCES GLOSSARY(DOCID))";
		return createString;
	}

	public static String getForumTreePath() {
		String createString = "CREATE TABLE FORUM_TREE_PATH (" +
				" ANCESTOR INT NOT NULL, " +
				" DESCENDANT INT NOT NULL, " +
				" LENGTH int, " +
				" PRIMARY KEY(ANCESTOR, DESCENDANT), " +
				" FOREIGN KEY (ANCESTOR) REFERENCES POSTS(DOCID), " +
				" FOREIGN KEY (DESCENDANT) REFERENCES POSTS(DOCID))";
		return createString;
	}

	public static String getTopicsDDE() {
		String createString = "CREATE TABLE TOPICS( " + 
				" DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_TOPICS PRIMARY KEY, " +
				" DOCTYPE int, " +
				" AUTHOR nvarchar(32), " + 
				" THEME nvarchar(256), " +
				" CONTENT nvarchar(2048), " +
				" REGDATE datetime, " +
				" SIGN nvarchar(1600), " + 
				" SIGNEDFIELDS nvarchar(2048), " +
				" CITATIONINDEX int, " +
				" ISPUBLIC int, " + 
				" STATUS int, " + 
				" PARENTDOCID int, " +
				" PARENTDOCTYPE int, " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime," +
				" DEFAULTRULEID nvarchar(32), " +
				" LASTUPDATE datetime, " +
				" VIEWTEXT nvarchar(2048), " +
				" TOPICDATE datetime, " +
                " DDBID varchar(16)," +
				" FORM nvarchar(32)) ";
		return createString;
	}

	public static String getPostsDDE() {
		String createString = "CREATE TABLE POSTS( " + 
				" DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_POSTS PRIMARY KEY, " +
				" DOCTYPE int, " +
				" AUTHOR nvarchar(32), " + 
				" THEME nvarchar(256), " +
				" CONTENT nvarchar(2048), " +
				" REGDATE datetime, " +
				" SIGN nvarchar(1600), " + 
				" SIGNEDFIELDS nvarchar(2048), " +
				" CITATIONINDEX int, " +
				" ISPUBLIC int, " + 
				" STATUS int, " + 
				" PARENTDOCID int, " +
				" PARENTDOCTYPE int, " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime," +
				" DEFAULTRULEID nvarchar(32), " +
				" POSTDATE datetime, " +
				" VIEWTEXT nvarchar(2048), " +
				" LASTUPDATE datetime, " +
                " DDBID nvarchar(16)," +
				" FORM nvarchar(32)) ";
		return createString;
	}

	public static String getPatchesDDE() {
		String dde = "create table PATCHES(" +
				" ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_PATCHES PRIMARY KEY, " +
				" PROCESSEDTIME datetime, " +
				" HASH int, " +
				" DESCRIPTION nvarchar(512)," +
				" NAME nvarchar(64))";
		return dde;
	}

	public static String getTasksDDE(){
		String dde = "create table TASKS(" +
				"DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_TASKS PRIMARY KEY," +
				" LASTUPDATE datetime," +
				" PARENTDOCID int," +
				" PARENTDOCTYPE int," +
				" TASKAUTHOR nvarchar(64)," +
				" DBD int, " +
				" CATEGORY int, " +
				" AUTHOR nvarchar(32)," +
				" REGDATE datetime," +
				" TASKVN nvarchar(32)," +
				" TASKDATE datetime," +
				" CONTENT nvarchar(2048)," +
				" COMMENT nvarchar(164)," +
				" CONTROLTYPE int," +
				" CTRLDATE datetime," +
				" CYCLECONTROL int," +
				" ALLCONTROL int," +
				" VIEWTEXT nvarchar(2048), " +
				" VIEWICON nvarchar(16), " +
				" FORM nvarchar(32), " +
				" DOCTYPE int," +
				" TASKTYPE int," +
				" SYNCSTATUS int, " +
				" ISOLD int, " +
				" HAS_ATTACHMENT int, " +
				" DEL int," +
				" PROJECT int," +
				" HAR int," + 
				" DEFAULTRULEID nvarchar(32), " +
				" BRIEFCONTENT nvarchar(512), " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime, " +
				" SIGN nvarchar(1600)," +
				" APPS int, " +
				" CUSTOMER int, " +
				" DDBID nvarchar(16)," +
				" TOPICID INT," +
				" SIGNEDFIELDS nvarchar(1200), " +
				" FOREIGN KEY(TOPICID) REFERENCES TOPICS(DOCID) ON UPDATE CASCADE ON DELETE CASCADE)";
		return dde;
	}

	public static String getTasksExecutorsDDE(){		
		String dde = "create table TASKSEXECUTORS(ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_TASKSEXECUTORS PRIMARY KEY, " +
				" DOCID int," +
				" EXECUTOR nvarchar(256)," +
				" RESETDATE datetime," +
				" RESETAUTHOR nvarchar(32)," +
				" COMMENT nvarchar(164), " +
				" RESPONSIBLE int, " +
				" EXECPERCENT int, " +
				" FOREIGN KEY (DOCID) REFERENCES TASKS(DOCID) ON DELETE CASCADE)";
		return dde;
	}

	public static String getAuthorReadersDDE(String tableName, String mainTableNames, String docID){
		String dde = "CREATE TABLE " + tableName +
				" (ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_" + tableName + " PRIMARY KEY, " +
				" USERNAME nvarchar(32)," +
				"DOCID INT, FAVORITES INT, FOREIGN KEY (DOCID) REFERENCES " + mainTableNames + "(" + docID + ") ON DELETE CASCADE," +
				"CONSTRAINT " + tableName.substring(0, 3) + "_" + mainTableNames + "_USR_UNIQUE UNIQUE (USERNAME, DOCID))";
		return dde;
	}

	public static String getIndexDDE(String tableName, String colName){
		String dde = "CREATE INDEX " + tableName + "_" + colName + "_idx ON " + tableName + "(" + colName + ")";
		return dde;
	}

	public static String getProjecsDDE(){
		String dde = "create table PROJECTS(DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_PROJECTS PRIMARY KEY," +
				" LASTUPDATE datetime," +
				" AUTHOR nvarchar(32)," +
				" AUTOSENDAFTERSIGN SMALLINT," +
				" AUTOSENDTOSIGN SMALLINT," +
				" BRIEFCONTENT nvarchar(512)," +
				" CONTENTSOURCE nvarchar(512), " +
				" COORDSTATUS int," +
				" REGDATE datetime," +
				" PROJECTDATE datetime," +
				" VN nvarchar(16),"+
				" VNNUMBER int,"+
				" DOCVERSION int," +
				" ISREJECTED int," +
				" RECIPIENT nvarchar(512)," +				
				" DOCTYPE int, " +
				" VIEWTEXT nvarchar(2048), " +
				" VIEWICON nvarchar(16), " +
				" FORM nvarchar(32), " +
				" SYNCSTATUS int," +
				" DOCFOLDER nvarchar(64)," +
				" DELIVERYTYPE nvarchar(64)," +
				" SENDER nvarchar(32), " +
				" NOMENTYPE int," +
				" HAS_ATTACHMENT int," +
				" DEL int," +
				" REGDOCID int," +
				" HAR int, " +
				" PROJECT int, " +
				" DEFAULTRULEID nvarchar(32), " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime, " +
				" SIGN nvarchar(1600)," +
				" SIGNEDFIELDS nvarchar(1200)," +
				" ORIGIN nvarchar(1024), " +
				" COORDINATS nvarchar(256), " +
				" CITY int, " +
				" STREET nvarchar(256), " +
				" HOUSE nvarchar(256), " +
				" PORCH nvarchar(256), " +
				" FLOOR nvarchar(256), " +
				" APARTMENT nvarchar(256), " +
				" RESPONSIBLE nvarchar(32), " +
				" CTRLDATE datetime, " +
				" SUBCATEGORY int, " +
				" CONTRAGENT nvarchar(256), " +
				" PODRYAD nvarchar(256), " +
				" SUBPODRYAD nvarchar(256), " +
				" EXECUTOR nvarchar(32), " +
				" TOPICID INT, " +
				" CATEGORY int, " +
				" PARENTDOCID int, " +
				" PARENTDOCTYPE int, " +
				" RESPOST nvarchar(256), " +
                " AMOUNTDAMAGE VARCHAR(1024), " +
				" DDBID nvarchar(16)," +
				" FOREIGN KEY(REGDOCID) REFERENCES MAINDOCS(DOCID) , " +
				" FOREIGN KEY(TOPICID) REFERENCES TOPICS(DOCID) ON UPDATE CASCADE ON DELETE CASCADE)";
		return dde;
	}

	public static String getExecutionsDDE(){
		String dde = "create table EXECUTIONS(DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_EXECUTIONS PRIMARY KEY," +
				" LASTUPDATE datetime," +
				" AUTHOR nvarchar(32)," +
				" REGDATE datetime," +
				" EXECUTOR nvarchar(32)," +
				" REPORT nvarchar(2048)," +
				" FINISHDATE datetime, " +
				" PARENTDOCID int," +
				" PARENTDOCTYPE int," +
				" VIEWTEXT nvarchar(2048), " +
				" VIEWICON nvarchar(16), " +
				" DOCTYPE int," +
				" FORM nvarchar(32), " +
				" SYNCSTATUS int, " +
				" NOMENTYPE int, " +
				" HAS_ATTACHMENT int, " +
				" DEFAULTRULEID nvarchar(32), " +
				" DEL int, " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime, " +
				" SIGN nvarchar(1600)," +
				" DDBID nvarchar(16)," +
				" SIGNEDFIELDS nvarchar(1200))";
		return dde;
	}

	public static String getCustomBlobsDDE(String mainTableName){
		String dde = "create table CUSTOM_BLOBS_" + mainTableName + " (ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_CUSTOM_BLOBS_" + mainTableName + " PRIMARY KEY, " +
				"DOCID int," +
				"NAME nvarchar(32)," +
				"TYPE int, " +
				"ORIGINALNAME nvarchar(128), " +
				"CHECKSUM nvarchar(40), " +
				"COMMENT varchar(256), " +
				"VALUE varbinary(max), " +
                "REGDATE datetime " +
				//",FOREIGN KEY (DOCID) REFERENCES " + mainTableName + "(DOCID) ON DELETE CASCADE" +
                ")";
		return dde;
	}

	public static String getCustomBlobsDDEForStruct(String mainTableName) {
		String dde = "create table CUSTOM_BLOBS_" + mainTableName + " (ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_CUSTOM_BLOBS" + mainTableName + " PRIMARY KEY, " +
				"DOCID int," +
				"NAME nvarchar(32)," +
				"TYPE int, " +
				"ORIGINALNAME nvarchar(128), " +
				"CHECKSUM nvarchar(40), " +
				"VALUE varbinary(max), " +
                "REGDATE datetime, " +
				"FOREIGN KEY (DOCID) REFERENCES " + mainTableName + "(EMPID) ON DELETE CASCADE)"; 
		return dde;
	}

	public static String getCustomFieldsDDE(){
		String dde="create table CUSTOM_FIELDS(ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_CUSTOM_FIELDS PRIMARY KEY," +
				"DOCID int, " +
				"NAME nvarchar(32), " +
				"VALUE nvarchar(2048), " +
				"TYPE int, " +
				"VALUEASDATE datetime, " +
				"VALUEASNUMBER numeric(19,4), " +
				"VALUEASGLOSSARY int, " +
				"VALUEASOBJECT xml, " +
				"VALUEASCLOB text, " +
				"FOREIGN KEY (DOCID) REFERENCES MAINDOCS(DOCID) ON DELETE CASCADE, " +
				"FOREIGN KEY (VALUEASGLOSSARY) REFERENCES GLOSSARY(DOCID) , " +
				"CONSTRAINT CUSTOM_FIELDS_UIQ UNIQUE (DOCID, NAME, VALUE))";
		return dde;
	}

	public static String getCountersTableDDE(){
		String dde = "create table COUNTERS(ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_COUNTERS PRIMARY KEY," +
				" KEYS nvarchar(32) CONSTRAINT COUNTERS_KEYS UNIQUE," +
				" LASTNUM int)";
		return dde;
	}

	public static String getGlossaryDDE(){
		String dde = "create table GLOSSARY(" +
				" DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_GLOSSARY PRIMARY KEY, " +
				" AUTHOR nvarchar(32), " +
				" PARENTDOCID int," +
				" PARENTDOCTYPE int," +
				" REGDATE datetime, " +
				" DOCTYPE int, " +
				" LASTUPDATE datetime," +
				" VIEWTEXT nvarchar(512)," +
				" VIEWICON nvarchar(16), " +
				" FORM nvarchar(32), " +
				" RANK int, " +
				" SYNCSTATUS int," +
				" DEFAULTRULEID nvarchar(32), " +
				" DEL int, " +
                " HAS_ATTACHMENT int, " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime, " +
                " TOPICID bigint, " +
				" SIGN nvarchar(1600)," +
				" DDBID nvarchar(16)," +
                " PARENTDOCDDBID nvarchar(16)," +
                " APPID nvarchar(16), " +
				" SIGNEDFIELDS nvarchar(1200))";
		return dde;
	}

	public static String getCustomFieldGlossary(){
		String dde = "create table CUSTOM_FIELDS_GLOSSARY(" +
				" ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_CUSTOM_FIELDS_GLOSSARY PRIMARY KEY," +
				" DOCID int REFERENCES GLOSSARY(DOCID) ON DELETE CASCADE," +
				" NAME nvarchar(32)," +
				" VALUE nvarchar(512)," +
				" TYPE int," +
				" VALUEASDATE datetime," +
				" VALUEASNUMBER numeric(19,4)," +
				" VALUEASGLOSSARY int)";
		return dde;
	}

	public static String getProjectRecipientsDDE(){
		String dde = "CREATE TABLE PROJECTRECIPIENTS(" +
				" ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_PROJECTRECIPIENTS PRIMARY KEY," +
				" DOCID int," +
				" RECIPIENT nvarchar(32)," +
				" FOREIGN KEY (DOCID) REFERENCES PROJECTS(DOCID) ON DELETE CASCADE)";
		return dde;
	}

	public static String getCoordBlockDDE(){
		String dde = "CREATE TABLE COORDBLOCKS(" +
				" ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_COORDBLOCKS PRIMARY KEY," +
				" DOCID int," +
				" TYPE int," +
				" DELAYTIME int," +
				" BLOCKNUMBER int," +
				" STATUS int," + 
				" COORDATE datetime, " +
				" FOREIGN KEY (DOCID) REFERENCES MAINDOCS(DOCID) ON DELETE CASCADE)";
		return dde;
	}

	public static String getCoordinatorsDDE(){
		String dde = "create table COORDINATORS(" +
				" ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_COORDINATORS PRIMARY KEY," +
				" BLOCKID int," +
				" COORDTYPE int," +
				" COORDINATOR nvarchar(256)," + 
				" COORDNUMBER int," +
				" DECISION int," +
				" COMMENT nvarchar(1024)," +
				" ISCURRENT int," +
				" DECISIONDATE datetime," +
				" COORDATE datetime," +
				" FOREIGN KEY (BLOCKID) REFERENCES COORDBLOCKS(ID) ON DELETE CASCADE)";
		return dde;		
	}

	public static String getOrganizationDDE(){
		String dde = "create table ORGANIZATIONS(" +
				" ORGID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_ORGANIZATIONS PRIMARY KEY," +
				getSystemFragment("ORG") +
				" FULLNAME nvarchar(256)," +
				" SHORTNAME nvarchar(48)," + 
				" ADDRESS nvarchar(128)," +
				" DEFAULTSERVER nvarchar(128)," +
				" COMMENT nvarchar(164)," +
				" ISMAIN int," +
                " BIN varchar(12)," +
				" DEL int)";

		return dde;	
	}

	public static String getDepartmentsDelCascadeTrigger(){
		String dde = "create trigger depdelcascadetrigger " +
				" on departments " +
				" for delete " +
				" as " +
				" delete departments" + 
				" from departments, deleted " +
				" where departments.mainid = deleted.depid" + 
				" delete organizations" + 
				" from organizations, deleted" + 
				" where organizations.orgid = deleted.orgid" +
				" delete employers" + 
				" from employers, deleted" + 
				" where employers.empid = deleted.empid";
		return dde;
	}

	public static String getDepartmentDDE(){
		String dde = "create table DEPARTMENTS(" +
				" DEPID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_DEPARTMENTS PRIMARY KEY," +		
				" ORGID int," +
				" EMPID int," +
				" MAINID int," +
				getSystemFragment("DEP") +
				" FULLNAME nvarchar(128)," +
				" SHORTNAME nvarchar(64)," + 
				" COMMENT nvarchar(164)," +
				" HITS int," + 
				" INDEXNUMBER nvarchar(32)," +
				" RANK int," +
				" TYPE int," +
				" DEL int," +
				" FOREIGN KEY (ORGID) REFERENCES ORGANIZATIONS(ORGID) ON DELETE NO ACTION," +
				" FOREIGN KEY (MAINID) REFERENCES DEPARTMENTS(DEPID) ON DELETE NO ACTION," +
				" CHECK (ORGID IS NOT NULL OR EMPID IS NOT NULL OR MAINID IS NOT NULL))";
		return dde;		
	}

	/* */

	public static String getRolesDDE() {
		String dde = "CREATE TABLE ROLES (" +
				" ROLEID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_ROLES PRIMARY KEY," +
				getSystemFragment("ROLES") +
				" ROLENAME nvarchar(32) UNIQUE NOT NULL," +
				" DESCRIPTION nvarchar(256)," +
				" DEFAULTRULEID nvarchar(32), " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime)";
		return dde;
	}

	public static String getUserRolesDDE() {
		String dde = "CREATE TABLE USER_ROLES (" +
				" UID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_USER_ROLES PRIMARY KEY," +
				" EMPID int NOT NULL," +
				" NAME varchar(32)," +
				" TYPE int NOT NULL," +
                " APPID varchar(256), " +
				" UNIQUE (EMPID, NAME, TYPE, APPID))";
		return dde;
	}

	public static String getEmployerDDE(){
		String dde = "create table EMPLOYERS(" +
				" EMPID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_EMPLOYERS PRIMARY KEY," +
				" DEPID int," +
				" ORGID int," +
				" BOSSID int," +
				getSystemFragment("EMP") +
				" FULLNAME nvarchar(128)," +
				" SHORTNAME nvarchar(64)," + 
				//	" USERID nvarchar(128) UNIQUE NOT NULL CHECK USERID NOT LIKE ''," +
				" USERID nvarchar(128) UNIQUE NOT NULL," +
				" COMMENT nvarchar(164)," +
				" RANK int," + 
				" HITS int," +
				" HAS_ATTACHMENT int," +
				" POST int," + 
				" ISBOSS int," +
				" INDEXNUMBER nvarchar(32)," +
				" PHONE nvarchar(128)," +
				" SENDTO int," +
				" DEL int," +
				" OBL int, " +
                " BIRTHDATE datetime, " +
				" REGION int, " +
				" VILLAGE int, " +
                " STATUS int, " +
				" FOREIGN KEY (DEPID) REFERENCES DEPARTMENTS(DEPID) ON DELETE NO ACTION," +
				" FOREIGN KEY (ORGID) REFERENCES ORGANIZATIONS(ORGID) ON DELETE NO ACTION," +
				" FOREIGN KEY (BOSSID) REFERENCES EMPLOYERS(EMPID) ON DELETE NO ACTION," +
				" FOREIGN KEY (POST) REFERENCES GLOSSARY(DOCID) ," +
				" CHECK (DEPID IS NOT NULL OR ORGID IS NOT NULL OR BOSSID IS NOT NULL))";
		return dde;
	}

	public static String getEmployersDelCascadeTrigger(){
		String dde = "create trigger empdelcascadetrigger " +
				" on employers " +
				" for delete " +
				" as " +
				" delete employers" + 
				" from employers, deleted " +
				" where employers.bossid = deleted.empid" + 
				" delete organizations" + 
				" from organizations, deleted" + 
				" where organizations.orgid = deleted.orgid" +
				" delete departments" + 
				" from departments, deleted" + 
				" where departments.depid = deleted.depid";
		return dde;
	}

	public static String getDepartmentsAlternationDDE() {
		String dde = "alter table DEPARTMENTS" +
				" ADD FOREIGN KEY (EMPID) REFERENCES EMPLOYERS(EMPID) ON DELETE CASCADE";
		return dde;
	}

	public static String getQueueDDE(){
		String dde = "create table QUEUE(" +
				" ID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_QUEE PRIMARY KEY," +
				" AUTHOR nvarchar(32)," +
				" REGDATE datetime," +
				" DOCID int," +
				" DOCTYPE int, " +
				" AGENTSTART nvarchar(64)," +
				" ERRORTEXT nvarchar(128)," +
				" ERRORTYPE int," +
				" ERRORTIME datetime," +
				" NUMOFATTEMPT int," +
				" PARAMETERS nvarchar(128))";

		return dde;		
	}

	public static String getCountAttachmentFunctionDDE() {
		String dde = "CREATE OR REPLACE FUNCTION set_attachment_flag()" +
				" RETURNS trigger as" +
				" $set_attachment_count$" +
				" DECLARE" +
				" docID integer;" +
				" idColName nvarchar;" +
				" mainTableName nvarchar;" +
				" mainColName nvarchar;" +
				" sourceRow record;" +
				" query nvarchar;" +
				" BEGIN" +
				" IF TG_OP = 'INSERT' THEN" +
				" sourceRow := NEW;" +
				" ELSE" +
				" sourceRow := OLD;" +
				" END IF;" +
				" IF upper(TG_TABLE_NAME) = 'CUSTOM_BLOBS_BOSS' OR upper(TG_TABLE_NAME) = 'CUSTOM_BLOBS_EMPLOYERS' THEN" +
				" docID := sourceRow.ID;" +
				" idColName := 'ID';" +
				" mainColName := 'EMPID';" +
				" ELSE" +
				" docID := sourceRow.DOCID;" +
				" idColName := 'DOCID';" +
                " mainColName := 'DOCID';" +
				" END IF;" +
				" mainTableName := substring(TG_TABLE_NAME from 14 for (length(TG_TABLE_NAME) - 13));" +
				" query := 'UPDATE ' || mainTableName || ' SET HAS_ATTACHMENT = (SELECT COUNT(*) FROM ' || TG_TABLE_NAME || ' WHERE ' || TG_TABLE_NAME || '.' || idColName || ' = ' || docID || ') WHERE ' || mainTableName || '.' || mainColName || ' = ' || docID;" +
				" EXECUTE(query);" +
				" RETURN NULL;" +
				" END;" +
				" $set_attachment_count$ LANGUAGE plpgsql";
		return dde;
	}

	public static String delAttachmentTriggerDDE(int docType) {
		String mainTableName = DatabaseUtil.getMainTableName(docType);
		String dde = "IF OBJECT_ID('dbo.set_attachment_trigger_" + mainTableName + "', 'TR') IS NOT NULL " +
				" DROP TRIGGER dbo.set_attachment_trigger_" + mainTableName;
		return dde;
	}

	public static String delResponsesTriggerDDE(String mainTableName) {
		return "IF OBJECT_ID('dbo.set_response_trigger_" + mainTableName + "', 'TR') IS NOT NULL " +
				" DROP TRIGGER dbo.set_response_trigger_" + mainTableName;
	}

	public static String getResponsesTriggerDDE(String mainTableName) {
		return "CREATE TRIGGER set_response_trigger_" + mainTableName + "\n" +
				" ON " + mainTableName + "\n" +
				" AFTER INSERT, DELETE \n" +
				" AS \n" +
				" IF EXISTS( SELECT * FROM INSERTED) \n" +
				" BEGIN \n" +
				" DECLARE @inid varchar \n" +
				" SET @inid = (select TOP 1 parentdocddbid from inserted) \n" +
				" update " + mainTableName + " set has_response = (select count(*) from " + mainTableName + " as ctn where ctn.parentdocddbid = @inid) where " + mainTableName + ".ddbid = @inid \n" +
				" END \n" +
				" IF EXISTS (SELECT * FROM DELETED) \n" +
				" BEGIN \n" +
				" DECLARE @delid varchar \n" +
				" SET @delid = (select TOP 1 parentdocddbid from deleted) \n" +
				" update " + mainTableName + " set has_response = (select count(*) from " + mainTableName + " as ctn where ctn.parentdocddbid = @delid) where " + mainTableName + ".ddbid = @delid \n" +
				" END  ";
	}

	public static String getAttachmentTriggerDDE(int docType) {
		String columnName = DatabaseUtil.getPrimaryKeyColumnName(docType);
		String blobTableName = DatabaseUtil.getCustomBlobsTableName(docType);
		String mainTableName = DatabaseUtil.getMainTableName(docType);
		String dde = " CREATE TRIGGER set_attachment_trigger_" + mainTableName +
				" ON " + blobTableName +
				" AFTER INSERT, DELETE " +
				" AS " +
				" IF EXISTS (SELECT * FROM INSERTED) " +
				" BEGIN " +
				" DECLARE @inid integer " +
				" SET @inid = (select TOP 1 docid from inserted)	" +		
				" update " + mainTableName + " set has_attachment = (select count(*) from " + blobTableName + " where docid = @inid) where " + columnName + " = @inid " +
				" END " +
				" IF EXISTS (SELECT * FROM DELETED) " +
				" BEGIN " +
				" DECLARE @delid integer " +
				" SET @delid = (select TOP 1 docid from DELETED) " +		
				" update " + mainTableName + " set has_attachment = (select count(*) from " + blobTableName + " where docid = @delid) where " + columnName + " = @delid " +
				" END ";
		return dde;
	}

	public static String getDBVersionTableDDE(){
		String dde = "create table DBVERSION(DOCID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_DBVERSION PRIMARY KEY," +
				"OLDVERSION int, " +
				"VERSION int, " +
				"UPDATEDATE datetime)";
		return dde;
	}

    public static String getStructureTreePath() {
        String createString = "CREATE TABLE STRUCTURE_TREE_PATH (" +
                " ANCESTOR VARCHAR(16) NOT NULL, " +
                " DESCENDANT VARCHAR(16) NOT NULL, " +
                " LENGTH int, " +
                " PRIMARY KEY(ANCESTOR, DESCENDANT))";
        return createString;
    }

    public static String getStructureView() {
        String createString = "create view structure as " +
                "select o.ddbid, o.viewtext from organizations o " +
                "union " +
                "select e.ddbid, e.viewtext from employers e " +
                "union " +
                "select d.ddbid, d.viewtext from departments d ";
        return createString;
    }

    public static String getStructureCollectionView() {
        return "create view structurecollection as (\n" +
                "select 0 as empid, 0 as depid, o.orgid, o.orgid as docid, o.regdate, o.author, o.doctype, o.parentdocid, o.parentdoctype, o.viewtext, o.ddbid, o.form, o.fullname, o.shortname,     o.address,     o.defaultserver, o.comment, o.ismain, o.bin,  0 as hits, '' as indexnumber, 0 as rank, 0 as type,\n" +
                "'' as userid, 0 as post,  '' as phone, getdate() as birthdate, \n" +
                "o.viewtext1, o.viewtext2, o.viewtext3, o.viewtext4, o.viewtext5, o.viewtext6, o.viewtext7, o.viewnumber, o.viewdate from organizations o\n" +
                "union\n" +
                "select 0 as empid, d.depid, 0 as orgid, d.depid as docid, d.regdate, d.author, d.doctype, d.parentdocid, d.parentdoctype, d.viewtext, d.ddbid, d.form, d.fullname, d.shortname, '' as address, '' as defaultserver, d.comment, 0 as ismain, '' as bin, d.hits, d.indexnumber, d.rank, d.type, \n" +
                "'' as userid, 0 as post,  '' as phone, getdate() as birthdate, \n" +
                "d.viewtext1, d.viewtext2, d.viewtext3, d.viewtext4, d.viewtext5, d.viewtext6, d.viewtext7, d.viewnumber, d.viewdate from departments d\n" +
                "union \n" +
                "select e.empid, 0 as depid, 0 as orgid, e.empid as docid, e.regdate, e.author, e.doctype, e.parentdocid, e.parentdoctype, e.viewtext, e.ddbid, e.form, e.fullname, e.shortname, '' as address, '' as defaultserver, e.comment, 0 as ismain, '' as bin, e.hits, e.indexnumber, e.rank, 0 as type, \n" +
                "e.userid, e.post, e.phone, e.birthdate,  \n" +
                "e.viewtext1, e.viewtext2, e.viewtext3, e.viewtext4, e.viewtext5, e.viewtext6, e.viewtext7, e.viewnumber, e.viewdate from employers e\n" +
                ")\n";
    }

	private static String getSystemFragment(String constraintNamePrefix){
		return " AUTHOR nvarchar(32)," +	
				" REGDATE datetime," +
				" DOCTYPE int, " +
				" LASTUPDATE datetime, " +
				" PARENTDOCID int," +
				" PARENTDOCTYPE int," +
				" VIEWTEXT nvarchar(512), " +
				" VIEWICON nvarchar(16), " +
				" DDBID nvarchar(16), " +
				" FORM nvarchar(32), " +
				" SYNCSTATUS int, ";		
	}

	public static String getFilterDDE() {
		String dde = "CREATE TABLE FILTER (id INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_FILTER PRIMARY KEY, " +
				" userid nvarchar(128) NOT NULL, " +
				" name nvarchar(128) NOT NULL, " +
				" enable int not null, " +
				" CONSTRAINT filter_userid_fkey FOREIGN KEY (userid) " +
				" REFERENCES employers (userid))";
		return dde;
	}

	public static String getConditionDDE() {
		String dde = "CREATE TABLE condition ( fid integer not null, " +
				" name nvarchar(128) NOT NULL, " +
				" value nvarchar(256) NOT NULL, " +
				" CONSTRAINT condition_fid_fkey FOREIGN KEY (fid) " +
				" REFERENCES filter (id) ON DELETE CASCADE)";
		return dde;
	}

	public static String getGroupsDDE(){
		String dde = "create table GROUPS(GROUPID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_GROUPS PRIMARY KEY, " +
				" GROUPNAME nvarchar(32) UNIQUE NOT NULL, " + 
				" FORM nvarchar(32), " +
				" DESCRIPTION nvarchar(256), " +
				" OWNER nvarchar(32), " +
				" TYPE int," +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " VIEWTEXT nvarchar(2048), " +
				" DEFAULTRULEID nvarchar(32), " +
				" VIEWTEXT1 nvarchar(256)," +
				" VIEWTEXT2 nvarchar(256)," +
				" VIEWTEXT3 nvarchar(256)," +
                " VIEWTEXT4 nvarchar(128)," +
                " VIEWTEXT5 nvarchar(128)," +
                " VIEWTEXT6 nvarchar(128)," +
                " VIEWTEXT7 nvarchar(128)," +
				" VIEWNUMBER numeric(19,4)," +
				" VIEWDATE datetime)";
		return dde;
	}

	public static String getUserGroupsDDE(){
		String dde = "create table USER_GROUPS(UID INT NOT NULL IDENTITY(1,1) CONSTRAINT PK_USER_GROUPS PRIMARY KEY, " + 
				" EMPID int NOT NULL," +
				" GROUPID int NOT NULL," +
				" TYPE int," +
				//" FOREIGN KEY (GROUPID) REFERENCES GROUPS(GROUPID) , " +
				" UNIQUE (EMPID, GROUPID))";
		return dde;
	}




	public static String createFullTextCatalog(){
		String dde = "CREATE FULLTEXT CATALOG ft AS DEFAULT;";		
		return dde;
	}

	public static String createFullTextIndex(int docType) {
		String tableName = DatabaseUtil.getMainTableName(docType);
		String dde = " CREATE FULLTEXT INDEX ON " + tableName + "(VIEWTEXT Language 1049, VIEWTEXT1 Language 1049, VIEWTEXT2 Language 1049, VIEWTEXT3 Language 1049, VIEWTEXT4 Language 1049, VIEWTEXT5 Language 1049, VIEWTEXT6 Language 1049, VIEWTEXT7 Language 1049) " +
				" KEY INDEX PK_" + tableName + 
				" WITH STOPLIST = SYSTEM;";
		return dde;
	}

	public static String createFullTextIndex(String tableName) {
		String dde = " CREATE FULLTEXT INDEX ON " + tableName + "(NAME Language 1049, VALUE Language 1049) " + 
				" KEY INDEX PK_" + tableName + 
				" WITH STOPLIST = SYSTEM;";
		return dde;
	}

	public static String getUpdateFTSTSVectorFunctionDDE() {
		String dde = "CREATE OR REPLACE FUNCTION update_fts_tsvector() \n" +
				" RETURNS trigger AS \n" +
				" $BODY$ \n" +
				" DECLARE \n" +
				"  tbl text := upper(TG_TABLE_NAME); \n" +
				" BEGIN \n" +
				"  IF (tbl = 'MAINDOCS' OR tbl = 'TASKS' OR tbl = 'EXECUTIONS' OR tbl = 'PROJECTS') THEN \n" +
				"    NEW.fts = setweight( coalesce( to_tsvector(NEW.viewtext),''),'A')||\n" +
				"      setweight( coalesce( to_tsvector(NEW.viewnumber::text),''),'B')||\n" +
				"      setweight( coalesce( to_tsvector(NEW.viewdate::text),''),'C');\n" +
				"  ELSEIF (tbl = 'CUSTOM_FIELDS') THEN \n" +
				"    NEW.fts = setweight( coalesce( to_tsvector(NEW.value::text),''),'A')||\n" +
				"      setweight( coalesce( to_tsvector(NEW.valueasnumber::text),''),'B')||\n" +
				"      setweight( coalesce( to_tsvector(NEW.valueasdate::text),''),'C');\n" +
				"  END IF;\n" +
				"  RETURN NEW;\n" +
				" END;\n" +
				" $BODY$ LANGUAGE plpgsql VOLATILE COST 100;\n" +
				" ALTER FUNCTION update_fts_tsvector() OWNER TO postgres;";
		return dde;
	}
}
