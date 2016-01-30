package kz.flabs.dataengine.oracle;

public class DDEScripts {
	public static String getDBVersionTableDDE() {
		String dde = "create table DBVERSION(DOCID numeric PRIMARY KEY, " + "OLDVERSION numeric, " + "VERSION numeric, "
				+ "UPDATEDATE DATE  DEFAULT SYSDATE )";
		return dde;
	}
}
