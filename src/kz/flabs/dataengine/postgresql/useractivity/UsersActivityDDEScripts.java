package kz.flabs.dataengine.postgresql.useractivity;

public class UsersActivityDDEScripts {

    public static String getUsersActivityDDE() {
        String dde = "create table USERS_ACTIVITY(ID BIGSERIAL NOT NULL PRIMARY KEY, " +
                " TYPE int NOT NULL," +
                " DBID varchar(32), " +
                " USERID varchar(32), " +
                " CLIENTIP char(15), " +
                " EVENTTIME timestamp," +
                " DOCID int NOT NULL," +
                " DOCTYPE int NOT NULL," +
                " DDBID varchar(16)," +
                " VIEWTEXT varchar(2048))";
        return dde;
    }

    public static String getUsersActivityChangesDDE() {
        String dde = "create table USERS_ACTIVITY_CHANGES(ID BIGSERIAL NOT NULL PRIMARY KEY, " +
                " AID bigint, " +
                " FIELDNAME  varchar(30), " +
                " OLDVALUE varchar(512), " +
                " NEWVALUE varchar(512), " +
                " FIELDTYPE int, " +
                " FOREIGN KEY (AID) REFERENCES USERS_ACTIVITY(ID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getUsersActivityBlobChangesDDE() {
        String dde = "create table USERS_ACTIVITY_BLOBCHANGES (ID BIGSERIAL NOT NULL PRIMARY KEY, " +
                "docid int, " +
                "doctype int, " +
                "name varchar(32), " +
                "type int, " +
                "originalname varchar(128), " +
                "cheksum varchar(40), " +
                "value bytea)";
        return dde;
    }

    public static String getRecycleBinDDE() {
        String dde = "create table recycle_bin (ID BIGSERIAL NOT NULL PRIMARY KEY, " +
                " aid bigint, " +
                " value bytea, " +
                " foreign key (aid) references users_activity(id))";
        return dde;
    }

    public static String getActivityDDE() {
        String dde = "CREATE TABLE activity \n" +
                "(\n" +
                "  id bigserial NOT NULL PRIMARY KEY,\n" +
                "  viewtext character varying(2048),\n" +
                "  userid character varying(256),\n" +
                "  parameters character varying(2048),\n" +
                "  ACTIVITY_TYPE boolean,\n" +
                "  RETURN_TIME timestamp ,\n" +
                "  EVENT_TIME timestamp , \n" +
                "  COMMENT character varying(1024),\n" +
                "  PROCESSED_REC integer,\n" +
                "  PROCESSED_SIZE integer,\n" +
                "  TRANSACTION character varying(1024),\n" +
                "  SERVICE_NAME character varying(1024),\n" +
                "  METHOD_NAME character varying(1024),\n" +
                "  REQUEST_TIME timestamp ,\n" +
                "  SPRING_SERVER character varying(1024)\n" +
                ")";
        return dde;
    }

}
