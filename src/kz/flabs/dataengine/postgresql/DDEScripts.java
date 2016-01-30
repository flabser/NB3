package kz.flabs.dataengine.postgresql;

public class DDEScripts {

    public static String getMainDocsDDE() {
        String createString = "CREATE TABLE MAINDOCS(" +
                " DOCID serial PRIMARY KEY, " +
                " AUTHOR varchar(32)," +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " REGDATE timestamp," +
                " DOCTYPE int, " +
                " LASTUPDATE timestamp, " +
                " VIEWTEXT varchar(2048), " +
                " VIEWICON varchar(16), " +
                " FORM varchar(32), " +
                " SYNCSTATUS int, " +
                " HAS_ATTACHMENT int, " +
                " HAS_RESPONSE int DEFAULT 0, " +
                " HAS_TOPIC boolean DEFAULT false, " +
                " DEFAULTRULEID varchar(32), " +
                " DEL int, " +
                getViewTextFragment() +
                " SIGN varchar(6144)," +
                " TOPICID bigint, " +
                " DDBID varchar(16)," +
                " PARENTDOCDDBID varchar(16)," +
                " APPID varchar(16), " +
                " SIGNEDFIELDS varchar(1200), " +
                " FTS TSVECTOR)";
        return createString;
    }

    public static String getForAcquaintViewIndexDDE() {
        return  "CREATE INDEX userid_docid_type_acquaint_view_index\n" +
                "  ON foracquaint\n" +
                "  USING btree\n" +
                "  (docid, userid COLLATE pg_catalog.\"default\");";
    }

    public static String getForAcquaintMaterializedViewDDE() {
        return "CREATE MATERIALIZED VIEW foracquaint\n" +
                "WITH (\n" +
                "  autovacuum_enabled=true\n" +
                ") AS \n" +
                " SELECT users_activity.docid,\n" +
                "    users_activity.userid\n" +
                "   FROM users_activity\n" +
                "  WHERE users_activity.type = 1001\n" +
                "WITH DATA;"; /*+
                "CREATE INDEX userid_docid_type_acquaint_view_index\n" +
                "  ON foracquaint\n" +
                "  USING btree\n" +
                "  (docid, userid COLLATE pg_catalog.\"default\");";*/
    }

    public static String getForAcquaintViewDDE() {
        return "CREATE VIEW foracquaint\n" +
                "AS \n" +
                " SELECT users_activity.docid,\n" +
                "    users_activity.userid\n" +
                "   FROM users_activity\n" +
                "  WHERE users_activity.type = 1001;\n"; /*+
                "CREATE INDEX userid_docid_type_acquaint_view_index\n" +
                "  ON foracquaint\n" +
                "  USING btree\n" +
                "  (docid, userid COLLATE pg_catalog.\"default\");";*/
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
        return "create or replace view structurecollection as (\n" +
                "select 0 as empid, 0 as depid, o.orgid, o.orgid as docid, o.regdate, o.author, o.doctype, o.parentdocid, o.parentdoctype, o.viewtext, o.ddbid, o.form, o.fullname, o.shortname,     o.address,     o.defaultserver, o.comment, o.ismain, o.bin,  0 as hits, '' as indexnumber, 0 as rank, 0 as type,\n" +
                "'' as userid, 0 as post,  '' as phone, now() as birthdate, \n" +
                "o.viewtext1, o.viewtext2, o.viewtext3, o.viewtext4, o.viewtext5, o.viewtext6, o.viewtext7, o.viewnumber, o.viewdate from organizations o\n" +
                "union\n" +
                "select 0 as empid, d.depid, 0 as orgid, d.depid as docid, d.regdate, d.author, d.doctype, d.parentdocid, d.parentdoctype, d.viewtext, d.ddbid, d.form, d.fullname, d.shortname, '' as address, '' as defaultserver, d.comment, 0 as ismain, '' as bin, d.hits, d.indexnumber, d.rank, d.type, \n" +
                "'' as userid, 0 as post,  '' as phone, now() as birthdate, \n" +
                "d.viewtext1, d.viewtext2, d.viewtext3, d.viewtext4, d.viewtext5, d.viewtext6, d.viewtext7, d.viewnumber, d.viewdate from departments d\n" +
                "union \n" +
                "select e.empid, 0 as depid, 0 as orgid, e.empid as docid, e.regdate, e.author, e.doctype, e.parentdocid, e.parentdoctype, e.viewtext, e.ddbid, e.form, e.fullname, e.shortname, '' as address, '' as defaultserver, e.comment, 0 as ismain, '' as bin, e.hits, e.indexnumber, e.rank, 0 as type, \n" +
                "e.userid, e.post, e.phone, e.birthdate,  \n" +
                "e.viewtext1, e.viewtext2, e.viewtext3, e.viewtext4, e.viewtext5, e.viewtext6, e.viewtext7, e.viewnumber, e.viewdate from employers e\n" +
                ")\n";
    }

    public static String getStructureTreePath() {
        String createString = "CREATE TABLE STRUCTURE_TREE_PATH (" +
                " ANCESTOR VARCHAR(16) NOT NULL, " +
                " DESCENDANT VARCHAR(16) NOT NULL, " +
                " LENGTH int, " +
                " PRIMARY KEY(ANCESTOR, DESCENDANT)) ";
        return createString;
    }

    public static String getSyncDDE() {
        String createString = "CREATE TABLE SYNC " +
                "( " +
                " REGDATE timestamp, " +
                " NEXTTIME timestamp, " +
                " CUTOFTIME timestamp, " +
                " TRIGGER int, " +
                " DOCCOUNT int, " +
                " TYPE int, " +
                " ERRORCOUNT int, " +
                " DESCRIPTION varchar(1024)" +
                ")";
        return createString;
    }

    public static String getGlossaryTreePath() {
        String createString = "CREATE TABLE GLOSSARY_TREE_PATH (" +
                " ANCESTOR BIGINT NOT NULL, " +
                " DESCENDANT BIGINT NOT NULL, " +
                " LENGTH int, " +
                " PRIMARY KEY(ANCESTOR, DESCENDANT), " +
                " FOREIGN KEY (ANCESTOR) REFERENCES GLOSSARY(DOCID), " +
                " FOREIGN KEY (DESCENDANT) REFERENCES GLOSSARY(DOCID))";
        return createString;
    }

    public static String getForumTreePath() {
        String createString = "CREATE TABLE FORUM_TREE_PATH (" +
                " ANCESTOR BIGINT NOT NULL, " +
                " DESCENDANT BIGINT NOT NULL, " +
                " LENGTH int, " +
                " PRIMARY KEY(ANCESTOR, DESCENDANT), " +
                " FOREIGN KEY (ANCESTOR) REFERENCES POSTS(DOCID), " +
                " FOREIGN KEY (DESCENDANT) REFERENCES POSTS(DOCID))";
        return createString;
    }

    public static String getTopicsDDE() {
        String createString = "CREATE TABLE TOPICS( " +
                " DOCID serial primary key, " +
                " DOCTYPE int, " +
                " AUTHOR varchar(32), " +
                " THEME varchar(256), " +
                " CONTENT varchar(2048), " +
                " REGDATE timestamp, " +
                " SIGN varchar(1600), " +
                " SIGNEDFIELDS varchar(2048), " +
                " CITATIONINDEX int, " +
                " ISPUBLIC int, " +
                " STATUS int, " +
                " PARENTDOCID int, " +
                " PARENTDOCTYPE int, " +
                getViewTextFragment() +
                " DEFAULTRULEID varchar(32), " +
                " LASTUPDATE timestamp, " +
                " VIEWTEXT varchar(2048), " +
                " TOPICDATE timestamp, " +
                " DDBID varchar(16)," +
                " FORM varchar(32)) ";
        return createString;
    }

    public static String getPostsDDE() {
        String createString = "CREATE TABLE POSTS( " +
                " DOCID serial primary key, " +
                " DOCTYPE int, " +
                " AUTHOR varchar(32), " +
                " THEME varchar(256), " +
                " CONTENT varchar(2048), " +
                " REGDATE timestamp, " +
                " SIGN varchar(1600), " +
                " SIGNEDFIELDS varchar(2048), " +
                " CITATIONINDEX int, " +
                " ISPUBLIC int, " +
                " STATUS int, " +
                " PARENTDOCID int, " +
                " PARENTDOCTYPE int, " +
                getViewTextFragment() +
                " DEFAULTRULEID varchar(32), " +
                " POSTDATE timestamp, " +
                " VIEWTEXT varchar(2048), " +
                " LASTUPDATE timestamp, " +
                " DDBID varchar(16)," +
                " FORM varchar(32)) ";
        return createString;
    }

    public static String getPatchesDDE() {
        String dde = "create table PATCHES(" +
                " ID serial PRIMARY KEY, " +
                " PROCESSEDTIME timestamp, " +
                " HASH int, " +
                " DESCRIPTION varchar(512)," +
                " NAME varchar(64))";
        return dde;
    }

    public static String getTasksDDE() {
        String dde = "create table TASKS(" +
                "DOCID serial PRIMARY KEY," +
                " LASTUPDATE timestamp," +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " TASKAUTHOR varchar(64)," +
                " AUTHOR varchar(32)," +
                " REGDATE timestamp," +
                " TASKVN varchar(32)," +
                " TASKDATE timestamp," +
                " CONTENT varchar(2048)," +
                " COMMENT varchar(164)," +
                " CONTROLTYPE int," +
                " CTRLDATE timestamp," +
                " DBD int DEFAULT 0," +
                " CYCLECONTROL int," +
                " ALLCONTROL int," +
                " VIEWTEXT varchar(2048), " +
                " VIEWICON varchar(16), " +
                " FORM varchar(32), " +
                " DOCTYPE int," +
                " TASKTYPE int," +
                " SYNCSTATUS int, " +
                " ISOLD int, " +
                " HAS_ATTACHMENT int, " +
                " DEL int," +
                " PROJECT int," +
                " HAR int," +
                " DEFAULTRULEID varchar(32), " +
                " BRIEFCONTENT varchar(512), " +
                getViewTextFragment() +
                " SIGN varchar(1600)," +
                " APPS int, " +
                " CUSTOMER int, " +
                " DDBID varchar(16)," +
                " TOPICID bigint," +
                " FTS TSVECTOR, " +
                " SIGNEDFIELDS varchar(1200), " +
                " CATEGORY int, " +
                " FOREIGN KEY(TOPICID) REFERENCES TOPICS(DOCID) ON UPDATE CASCADE ON DELETE CASCADE)";
        return dde;
    }

    public static String getTasksExecutorsDDE() {
        String dde = "create table TASKSEXECUTORS(ID serial PRIMARY KEY, " +
                " DOCID int," +
                " EXECUTOR varchar(256)," +
                " RESETDATE timestamp," +
                " RESETAUTHOR varchar(32)," +
                " COMMENT varchar(164), " +
                " RESPONSIBLE int, " +
                " EXECPERCENT int, " +
                " FOREIGN KEY (DOCID) REFERENCES TASKS(DOCID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getAuthorReadersDDE(String tableName, String mainTableNames, String docID) {
        String dde = "CREATE TABLE " + tableName +
                " (ID serial, " +
                " USERNAME VARCHAR(32)," +
                "DOCID INT, FAVORITES INT, FOREIGN KEY (DOCID) REFERENCES " + mainTableNames + "(" + docID + ") ON DELETE CASCADE," +
                "CONSTRAINT " + tableName.substring(0, 3) + "_" + mainTableNames + "_USR_UNIQUE UNIQUE (USERNAME, DOCID))";
        return dde;
    }

    public static String getIndexDDE(String tableName, String colName) {
        String dde = "CREATE INDEX " + tableName + "_" + colName + "_idx ON " + tableName + "(" + colName + ")";
        return dde;
    }

    public static String getProjecsDDE() {
        String dde = "create table PROJECTS(DOCID serial PRIMARY KEY," +
                " LASTUPDATE timestamp," +
                " AUTHOR varchar(32)," +
                " AUTOSENDAFTERSIGN SMALLINT," +
                " AUTOSENDTOSIGN SMALLINT," +
                " BRIEFCONTENT varchar(512)," +
                " CONTENTSOURCE varchar(512), " +
                " COORDSTATUS int," +
                " REGDATE timestamp," +
                " PROJECTDATE timestamp," +
                " VN varchar(16)," +
                " VNNUMBER int," +
                " DOCVERSION int," +
                " ISREJECTED int," +
                " RECIPIENT varchar(512)," +
                " DOCTYPE int, " +
                " VIEWTEXT varchar(2048), " +
                " VIEWICON varchar(16), " +
                " FORM varchar(32), " +
                " SYNCSTATUS int," +
                " DOCFOLDER varchar(64)," +
                " DELIVERYTYPE varchar(64)," +
                " SENDER varchar(32), " +
                " NOMENTYPE int," +
                " HAS_ATTACHMENT int," +
                " DEL int," +
                " REGDOCID int," +
                " HAR int, " +
                " PROJECT int, " +
                " DEFAULTRULEID varchar(32), " +
                getViewTextFragment() +
                " SIGN varchar(1600)," +
                " SIGNEDFIELDS varchar(1200)," +
                " ORIGIN varchar(1024), " +
                " COORDINATS varchar(256), " +
                " CITY int, " +
                " STREET varchar(256), " +
                " HOUSE varchar(256), " +
                " PORCH varchar(256), " +
                " FLOOR varchar(256), " +
                " APARTMENT varchar(256), " +
                " RESPONSIBLE varchar(32), " +
                " CTRLDATE timestamp, " +
                " SUBCATEGORY int, " +
                " CONTRAGENT varchar(256), " +
                " PODRYAD varchar(256), " +
                " SUBPODRYAD varchar(256), " +
                " EXECUTOR varchar(32), " +
                " DDBID varchar(16)," +
                " TOPICID bigint, " +
                " RESPOST varchar(256), " +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " CATEGORY int, " +
                " FTS TSVECTOR, " +
                " AMOUNTDAMAGE VARCHAR(1024), " +
                " DBD int DEFAULT 0," +
                " FOREIGN KEY(REGDOCID) REFERENCES MAINDOCS(DOCID) ON DELETE RESTRICT, " +
                " FOREIGN KEY(TOPICID) REFERENCES TOPICS(DOCID) ON UPDATE CASCADE ON DELETE CASCADE)";
        return dde;
    }

    public static String getExecutionsDDE() {
        String dde = "create table EXECUTIONS(DOCID serial PRIMARY KEY," +
                " LASTUPDATE timestamp," +
                " AUTHOR varchar(32)," +
                " REGDATE timestamp," +
                " EXECUTOR varchar(32)," +
                " REPORT varchar(2048)," +
                " FINISHDATE timestamp, " +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " VIEWTEXT varchar(2048), " +
                " VIEWICON varchar(16), " +
                " DOCTYPE int," +
                " FORM varchar(32), " +
                " SYNCSTATUS int, " +
                " NOMENTYPE int, " +
                " HAS_ATTACHMENT int, " +
                " DEFAULTRULEID varchar(32), " +
                " DEL int, " +
                getViewTextFragment() +
                " DDBID varchar(16)," +
                " FTS TSVECTOR, " +
                " SIGN varchar(1600)," +
                " SIGNEDFIELDS varchar(1200))";
        return dde;
    }

    public static String getCustomBlobsDDE(String mainTableName) {
        String dde = "create table CUSTOM_BLOBS_" + mainTableName + " (ID serial PRIMARY KEY, " +
                "DOCID int," +
                "NAME varchar(32)," +
                "TYPE int, " +
                "ORIGINALNAME varchar(128), " +
                "CHECKSUM varchar(40), " +
                "COMMENT varchar(256), " +
                "VALUE bytea, " +
                "VALUE_OID oid, " +
                "REGDATE timestamp)";
        return dde;
    }

    public static String getReadMarksDDE(String tableName) {
        String dde = "CREATE TABLE READMARKS_" + tableName + " (" +
                " markid serial," +
                " docid INT," +
                " userid VARCHAR(128)," +
                " FOREIGN KEY (docid) REFERENCES " + tableName + "(DOCID) ON DELETE CASCADE," +
                " UNIQUE(docid, userid))";
        return dde;
    }

    public static String getCustomBlobsDDEForStruct(String mainTableName) {
        String dde = "create table CUSTOM_BLOBS_" + mainTableName + " (ID serial primary key, " +
                "DOCID int," +
                "NAME varchar(32)," +
                "TYPE int, " +
                "ORIGINALNAME varchar(128), " +
                "CHECKSUM varchar(40), " +
                "VALUE bytea, " +
                "REGDATE timestamp, " +
                "FOREIGN KEY (DOCID) REFERENCES " + mainTableName + "(EMPID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getCustomFieldsDDE() {
        String dde = "create table CUSTOM_FIELDS(ID serial," +
                "DOCID int, " +
                "NAME varchar(32), " +
                "VALUE varchar(2048), " +
                "TYPE int, " +
                "VALUEASDATE timestamp, " +
                "VALUEASNUMBER numeric(19,4), " +
                "VALUEASGLOSSARY int, " +
                "VALUEASOBJECT xml, " +
                "VALUEASCLOB text, " +
                "FTS TSVECTOR, " +
                "FOREIGN KEY (DOCID) REFERENCES MAINDOCS(DOCID) ON DELETE CASCADE, " +
                "FOREIGN KEY (VALUEASGLOSSARY) REFERENCES GLOSSARY(DOCID) ON DELETE RESTRICT, " +
                "CONSTRAINT CUSTOM_FIELDS_UIQ UNIQUE (DOCID, NAME, VALUE))";
        return dde;
    }

    public static String getCountersTableDDE() {
        String dde = "create table COUNTERS(ID serial," +
                " KEYS varchar(32) CONSTRAINT COUNTERS_KEYS UNIQUE," +
                " LASTNUM int)";
        return dde;
    }

    public static String getGlossaryDDE() {
        String dde = "create table GLOSSARY(" +
                " DOCID serial PRIMARY KEY, " +
                " AUTHOR varchar(32), " +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " REGDATE TIMESTAMP, " +
                " DOCTYPE int, " +
                " LASTUPDATE TIMESTAMP," +
                " VIEWTEXT varchar(512)," +
                " VIEWICON varchar(16), " +
                " FORM varchar(32), " +
                " RANK int, " +
                " SYNCSTATUS int," +
                " DEFAULTRULEID varchar(32), " +
                " HAS_ATTACHMENT int, " +
                " DEL int, " +
                getViewTextFragment() +
                " SIGN varchar(1600)," +
                " TOPICID bigint, " +
                " DDBID varchar(16)," +
                " PARENTDOCDDBID varchar(16)," +
                " APPID varchar(16), " +
                " SIGNEDFIELDS varchar(1200))";
        return dde;
    }

    public static String getCustomFieldGlossary() {
        String dde = "create table CUSTOM_FIELDS_GLOSSARY(" +
                " ID serial," +
                " DOCID int REFERENCES GLOSSARY(DOCID) ON DELETE CASCADE," +
                " NAME varchar(32)," +
                " VALUE varchar(512)," +
                " TYPE int," +
                " VALUEASDATE timestamp," +
                " VALUEASNUMBER numeric(19,4)," +
                " VALUEASGLOSSARY int)";
        return dde;
    }

    public static String getProjectRecipientsDDE() {
        String dde = "CREATE TABLE PROJECTRECIPIENTS(" +
                " ID serial PRIMARY KEY," +
                " DOCID int," +
                " RECIPIENT varchar(32)," +
                " FOREIGN KEY (DOCID) REFERENCES PROJECTS(DOCID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getCoordBlockDDE() {
        String dde = "CREATE TABLE COORDBLOCKS(" +
                " ID serial PRIMARY KEY," +
                " DOCID int," +
                " TYPE int," +
                " DELAYTIME int," +
                " BLOCKNUMBER int," +
                " STATUS int," +
                " COORDATE timestamp, " +
                " FOREIGN KEY (DOCID) REFERENCES MAINDOCS(DOCID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getCoordinatorsDDE() {
        String dde = "create table COORDINATORS(" +
                " ID serial PRIMARY KEY," +
                " BLOCKID int," +
                " COORDTYPE int," +
                " COORDINATOR varchar(256)," +
                " COORDNUMBER int," +
                " DECISION int," +
                " COMMENT varchar(1024)," +
                " ISCURRENT int," +
                " DECISIONDATE timestamp," +
                " COORDATE timestamp," +
                " FOREIGN KEY (BLOCKID) REFERENCES COORDBLOCKS(ID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getOrganizationDDE() {
        String dde = "create table ORGANIZATIONS(" +
                " ORGID serial PRIMARY KEY," +
                getSystemFragment("ORG") +
                " FULLNAME varchar(256)," +
                " SHORTNAME varchar(48)," +
                " ADDRESS varchar(128)," +
                " DEFAULTSERVER varchar(128)," +
                " COMMENT varchar(164)," +
                " ISMAIN int," +
                " BIN varchar(12)," +
                getViewTextFragment() +
                " DEL int)";

        return dde;
    }

    private static String getViewTextFragment() {
        return  " VIEWTEXT1 varchar(256)," +
                " VIEWTEXT2 varchar(256)," +
                " VIEWTEXT3 varchar(256)," +
                " VIEWTEXT4 varchar(128)," +
                " VIEWTEXT5 varchar(128)," +
                " VIEWTEXT6 varchar(128)," +
                " VIEWTEXT7 varchar(128)," +
                " VIEWNUMBER numeric(19,4)," +
                " VIEWDATE timestamp, ";
    }

    public static String getDepartmentDDE() {
        String dde = "create table DEPARTMENTS(" +
                " DEPID serial PRIMARY KEY," +
                " ORGID int," +
                " EMPID int," +
                " MAINID int," +
                getSystemFragment("DEP") +
                " FULLNAME varchar(128)," +
                " SHORTNAME varchar(64)," +
                " COMMENT varchar(164)," +
                " HITS int," +
                " INDEXNUMBER varchar(32)," +
                " RANK int," +
                " TYPE int," +
                " DEL int," +
                getViewTextFragment() +
                " FOREIGN KEY (ORGID) REFERENCES ORGANIZATIONS(ORGID) ON DELETE CASCADE," +
                " FOREIGN KEY (MAINID) REFERENCES DEPARTMENTS(DEPID) ON DELETE CASCADE," +
                " CHECK (ORGID IS NOT NULL OR EMPID IS NOT NULL OR MAINID IS NOT NULL))";
        return dde;
    }

    public static String getRolesDDE() {
        String dde = "CREATE TABLE ROLES (" +
                " ROLEID serial PRIMARY KEY," +
                getSystemFragment("ROLES") +
                " ROLENAME varchar(32) UNIQUE NOT NULL," +
                " DESCRIPTION varchar(256)," +
                getViewTextFragment() +
                " DEFAULTRULEID varchar(32))";
        return dde;
    }

    public static String getUserRolesDDE() {
        String dde = "CREATE TABLE USER_ROLES (" +
                " UID serial PRIMARY KEY," +
                " EMPID int NOT NULL," +
                " NAME varchar(64)," +
                " TYPE int NOT NULL," +
                " APPID varchar(256), " +
                " UNIQUE (EMPID, NAME, TYPE, APPID))";
        return dde;
    }

    public static String getEmployerDDE() {
        String dde = "create table EMPLOYERS(" +
                " EMPID serial PRIMARY KEY," +
                " DEPID int," +
                " ORGID int," +
                " BOSSID int," +
                getSystemFragment("EMP") +
                " FULLNAME varchar(128)," +
                " SHORTNAME varchar(64)," +
                //	" USERID varchar(128) UNIQUE NOT NULL CHECK USERID NOT LIKE ''," +
                " USERID varchar(128) UNIQUE NOT NULL," +
                " COMMENT varchar(164)," +
                " RANK int," +
                " HITS int," +
                " HAS_ATTACHMENT int," +
                " POST int," +
                " ISBOSS int," +
                " INDEXNUMBER varchar(32)," +
                " PHONE varchar(128)," +
                " SENDTO int," +
                " DEL int," +
                " OBL int, " +
                " REGION int, " +
                " VILLAGE int, " +
                " BIRTHDATE timestamp, " +
                " STATUS int, " +
                getViewTextFragment() +
                " FOREIGN KEY (DEPID) REFERENCES DEPARTMENTS(DEPID) ON DELETE CASCADE," +
                " FOREIGN KEY (ORGID) REFERENCES ORGANIZATIONS(ORGID) ON DELETE CASCADE," +
                " FOREIGN KEY (BOSSID) REFERENCES EMPLOYERS(EMPID) ON DELETE CASCADE," +
                " FOREIGN KEY (POST) REFERENCES GLOSSARY(DOCID) ON DELETE RESTRICT," +
                " CHECK (DEPID IS NOT NULL OR ORGID IS NOT NULL OR BOSSID IS NOT NULL))";
        return dde;
    }

    public static String getDepartmentsAlternationDDE() {
        String dde = "alter table DEPARTMENTS" +
                " ADD FOREIGN KEY (EMPID) REFERENCES EMPLOYERS(EMPID) ON DELETE CASCADE";
        return dde;
    }

    public static String getQueueDDE() {
        String dde = "create table QUEUE(" +
                " ID serial PRIMARY KEY," +
                " AUTHOR varchar(32)," +
                " REGDATE timestamp," +
                " DOCID int," +
                " DOCTYPE int, " +
                " AGENTSTART varchar(64)," +
                " ERRORTEXT varchar(128)," +
                " ERRORTYPE int," +
                " ERRORTIME timestamp," +
                " NUMOFATTEMPT int," +
                " PARAMETERS varchar(128))";

        return dde;
    }

    public static String getCountResponsesFunctionDDE() {
        return "CREATE OR REPLACE FUNCTION set_response_flag()" +
                " RETURNS trigger as " +
                " $set_response_count$ " +
                " DECLARE " +
                " ddbid varchar; " +
                " sourceRow record; " +
                " query varchar; " +
                " BEGIN " +
                " if TG_OP = 'INSERT' THEN " +
                " sourceRow := NEW; " +
                " ELSE " +
                " sourceRow := OLD; " +
                " END IF; " +
                " ddbid := sourceRow.PARENTDOCDDBID; " +
                " IF ddbid is null THEN " +
                " ddbid := ''; " +
                " END IF; " +
                " query := 'UPDATE ' || TG_TABLE_NAME || ' SET HAS_RESPONSE = (SELECT COUNT(*) FROM ' || TG_TABLE_NAME || ' WHERE ' || TG_TABLE_NAME || '.PARENTDOCDDBID = ''' || ddbid || ''') WHERE ' || TG_TABLE_NAME || '.DDBID = ''' || ddbid || '''';" +
                " EXECUTE(query); " +
                " RETURN NULL; " +
                " END; " +
                " $set_response_count$ LANGUAGE plpgsql";
    }

    public static String getCountAttachmentFunctionDDE() {
        String dde = "CREATE OR REPLACE FUNCTION set_attachment_flag()" +
                " RETURNS trigger as" +
                " $set_attachment_count$" +
                " DECLARE" +
                " docID integer;" +
                " idColName varchar;" +
                " mainTableName varchar;" +
                " mainColName varchar;" +
                " sourceRow record;" +
                " query varchar;" +
                " BEGIN" +
                " IF TG_OP = 'INSERT' THEN" +
                " sourceRow := NEW;" +
                " ELSE" +
                " sourceRow := OLD;" +
                " END IF;" +
                " IF upper(TG_TABLE_NAME) = 'CUSTOM_BLOBS_BOSS' OR upper(TG_TABLE_NAME) = 'CUSTOM_BLOBS_EMPLOYERS' THEN" +
                " docID := sourceRow.ID;" +
                " idColName := 'ID';" +
                " mainColname := 'EMPID';" +
                " ELSE" +
                " docID := sourceRow.DOCID;" +
                " idColName := 'DOCID';" +
                " mainColname := 'DOCID';" +
                " END IF;" +
                " mainTableName := substring(TG_TABLE_NAME from 14 for (length(TG_TABLE_NAME) - 13));" +
                " query := 'UPDATE ' || mainTableName || ' SET HAS_ATTACHMENT = (SELECT COUNT(*) FROM ' || TG_TABLE_NAME || ' WHERE ' || TG_TABLE_NAME || '.' || idColName || ' = ' || docID || ') WHERE ' || mainTableName || '.' || mainColName || ' = ' || docID;" +
                " EXECUTE(query);" +
                " RETURN NULL;" +
                " END;" +
                " $set_attachment_count$ LANGUAGE plpgsql";
        return dde;
    }

    public static String getResponsesTriggerDDE(String mainTableName) {
        return "CREATE TRIGGER set_remove_resp_flag_" + mainTableName +
                " AFTER INSERT OR DELETE OR UPDATE ON " + mainTableName +
                " FOR EACH ROW EXECUTE PROCEDURE set_response_flag()";
    }

    public static String getForAcquaintTriggerDDE() {
        return "CREATE TRIGGER update_view_for_acquaint\n" +
                "  AFTER INSERT\n" +
                "  ON users_activity\n" +
                "  FOR EACH ROW\n" +
                "  EXECUTE PROCEDURE update_view_for_acquaint();";
    }

    public static String getForAcquaintFunctionDDE() {
        return "CREATE OR REPLACE FUNCTION update_view_for_acquaint()\n" +
                "  RETURNS trigger AS\n" +
                "$BODY$ DECLARE action_type integer; sourceRow record;\n" +
                "  BEGIN IF TG_OP = 'INSERT' THEN sourceRow := NEW; ELSE sourceRow := OLD; END IF;\n" +
                "  IF sourceRow.TYPE = 1001 THEN EXECUTE('REFRESH MATERIALIZED VIEW foracquaint'); END IF;\n" +
                "  RETURN NULL;\n" +
                "  END;\n" +
                "  $BODY$\n" +
                "  LANGUAGE plpgsql VOLATILE";
    }

    public static String getAttachmentTriggerDDE(String mainTableName) {
        String dde = "CREATE TRIGGER set_remove_att_flag_" + mainTableName +
                " AFTER INSERT OR DELETE OR UPDATE ON CUSTOM_BLOBS_" + mainTableName +
                " FOR EACH ROW EXECUTE PROCEDURE set_attachment_flag()";
        return dde;
    }

    public static String getDBVersionTableDDE() {
        String dde = "create table DBVERSION(DOCID serial PRIMARY KEY, " +
                "OLDVERSION int, " +
                "VERSION int, " +
                "UPDATEDATE timestamp)";
        return dde;
    }

    private static String getSystemFragment(String constraintNamePrefix) {
        return " AUTHOR varchar(32)," +
                " REGDATE timestamp," +
                " DOCTYPE int, " +
                " LASTUPDATE timestamp, " +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " VIEWTEXT varchar(512), " +
                " VIEWICON varchar(16), " +
                " DDBID varchar(16), " +
                " FORM varchar(32), " +
                " SYNCSTATUS int, ";
    }

    public static String getFilterDDE() {
        String dde = "CREATE TABLE filter (id serial PRIMARY KEY, " +
                " userid varchar(128) NOT NULL, " +
                " name varchar(128) NOT NULL, " +
                " enable int not null, " +
                " CONSTRAINT filter_userid_fkey FOREIGN KEY (userid) " +
                " REFERENCES employers (userid))";
        return dde;
    }

    public static String getConditionDDE() {
        String dde = "CREATE TABLE condition ( fid integer not null, " +
                " name varchar(128) NOT NULL, " +
                " value varchar(256) NOT NULL, " +
                " CONSTRAINT condition_fid_fkey FOREIGN KEY (fid) " +
                " REFERENCES filter (id) ON DELETE CASCADE)";
        return dde;
    }

    public static String getGroupsDDE() {
        String dde = "create table GROUPS(GROUPID serial PRIMARY KEY, " +
                " GROUPNAME varchar(32) UNIQUE NOT NULL, " +
                " FORM varchar(32), " +
                " DESCRIPTION varchar(256), " +
                " OWNER varchar(32), " +
                " TYPE int," +
                " PARENTDOCID int," +
                " PARENTDOCTYPE int," +
                " VIEWTEXT varchar(2048), " +
                getViewTextFragment() +
                " DEFAULTRULEID varchar(32))";
        return dde;
    }

    public static String getUserGroupsDDE() {
        String dde = "create table USER_GROUPS(UID serial PRIMARY KEY, " +
                " EMPID int NOT NULL," +
                " GROUPID int NOT NULL," +
                " TYPE int," +
                //" FOREIGN KEY (GROUPID) REFERENCES GROUPS(GROUPID) ON DELETE RESTRICT, " +
                " UNIQUE (EMPID, GROUPID))";
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

    public static String getDiscussionFlagFunction() {
        return "CREATE OR REPLACE FUNCTION set_discussion_flag()\n" +
                "  RETURNS trigger AS\n" +
                "$BODY$ DECLARE \n" +
                "  docID INTEGER;\n" +
                "  idColName            VARCHAR;\n" +
                "  mainTableName        VARCHAR;\n" +
                "  mainColName          VARCHAR;\n" +
                "  sourceRow            record;\n" +
                "  query                VARCHAR;\n" +
                "BEGIN IF TG_OP = 'INSERT'\n" +
                "  THEN sourceRow := NEW;\n" +
                "  ELSE sourceRow := OLD;\n" +
                " END IF;\n" +
                "  mainTableName := 'MAINDOCS';\n" +
                "  idColName := 'PARENTDOCID';\n" +
                "  mainColName := 'DOCID';\n" +
                "  docID := sourceRow.PARENTDOCID;\n" +
                "  query := 'UPDATE ' || mainTableName || ' SET HAS_TOPIC = CASE (SELECT COUNT(DOCID) FROM ' || TG_TABLE_NAME || ' WHERE ' || TG_TABLE_NAME || '.' || idColName || ' = ' || docID || ') WHEN 0 THEN FALSE ELSE TRUE END WHERE ' || mainTableName || '.' || mainColName || ' = ' || docID;\n" +
                "  --raise exception 'Sql query: %', query; \n" +
                "  EXECUTE(query);\n" +
                "  RETURN NULL;\n" +
                "END; $BODY$\n" +
                "  LANGUAGE plpgsql VOLATILE";
    }

    public static String getDiscussionFlagTrigger() {
        return "CREATE TRIGGER set_remove_discussion_flag_maindocs\n" +
                "  AFTER INSERT OR UPDATE OR DELETE\n" +
                "  ON topics\n" +
                "  FOR EACH ROW\n" +
                "  EXECUTE PROCEDURE set_discussion_flag();";
    }
}
