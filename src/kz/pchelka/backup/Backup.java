package kz.pchelka.backup;

import kz.flabs.servlets.admin.IAdministartorForm;
import kz.flabs.util.Util;

import java.io.File;
import java.util.Calendar;

public class Backup implements IAdministartorForm{

	private final BackupList backupList;
	private File bDir;
	private File[] list = new File[0];
	private String id;
	private String controlSum;
	private String app;
	private Calendar creationDate;

    public Backup(BackupList backupList, String id, String app) {
        this.id = id;
        this.backupList = backupList;
        bDir = new File(this.backupList.dir.getAbsolutePath() + File.separator + id);
        if (bDir.isDirectory()) {
            list = bDir.listFiles();
        }
        this.app = app;
    }

    public int getCount() {
		return list.length;
	}

	public String toXML() {
		String cd = Util.convertDataTimeToString(creationDate);
		String xmlFragment = "<id>" + id + "</id>" +
				"<count>" + getCount() + "</count>" +
				"<controlsum>" + controlSum + "</controlsum>" +
				"<app>" + app + "</app>" +
				"<creationdate>" + cd + "</creationdate>";
		return xmlFragment;
	}
}