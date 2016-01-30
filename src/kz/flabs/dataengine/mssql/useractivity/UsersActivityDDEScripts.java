package kz.flabs.dataengine.mssql.useractivity;

public class UsersActivityDDEScripts {

    public static String getUsersActivityDDE() {
        String dde = "create table USERS_ACTIVITY(ID INT NOT NULL IDENTITY(1,1) PRIMARY KEY, " +
                " TYPE int NOT NULL," +
                " DBID varchar(32), " +
                " USERID varchar(32), " +
                " CLIENTIP char(15), " +
                " EVENTTIME datetime," +
                " DOCID int NOT NULL," +
                " VIEWTEXT nvarchar(2056), " +
                " DDBID nvarchar(16)," +
                " DOCTYPE int NOT NULL)";
        return dde;
    }

    public static String getUsersActivityChangesDDE() {
        String dde = "create table USERS_ACTIVITY_CHANGES(ID INT NOT NULL IDENTITY(1,1) PRIMARY KEY, " +
                " AID int, " +
                " FIELDNAME  varchar(30), " +
                " OLDVALUE varchar(512), " +
                " NEWVALUE varchar(512), " +
                " FIELDTYPE int, " +
                " FOREIGN KEY (AID) REFERENCES USERS_ACTIVITY(ID) ON DELETE CASCADE)";
        return dde;
    }

    public static String getUsersActivityBlobChangesDDE() {
        String dde = "create table USERS_ACTIVITY_BLOBCHANGES (id INT NOT NULL IDENTITY(1,1) PRIMARY KEY, " +
                "docid int, " +
                "doctype int, " +
                "name varchar(32), " +
                "type int, " +
                "originalname varchar(128), " +
                "cheksum varchar(40), " +
                "value varbinary)";
        return dde;
    }

    public static String getRecycleBinDDE() {
        String dde = "create table recycle_bin (id INT NOT NULL IDENTITY(1,1) PRIMARY KEY, " +
                " aid int, " +
                " value varbinary, " +
                " foreign key (aid) references users_activity(id))";
        return dde;
    }

}
