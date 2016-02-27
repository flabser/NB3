package kz.flabs.runtimeobj;

import kz.flabs.dataengine.Const;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.webrule.constants.FieldType;
import kz.lof.appenv.AppEnv;

public class Application {
	public String orgName;
	public String orgID;

	private int licenceCount;
	private AppEnv env;

	public Application(AppEnv env) {
		this.env = env;
		orgName = env.globalSetting.orgName;
		orgID = env.globalSetting.id;
	}

	public int getAllDocsCount() {
		return 0;

	}

	public int getOperationPeriodInDays() {
		return 0;

	}

	public int getLicenceCount() {
		return licenceCount;

	}

	public int getRemainLicenceCount() {
		return 0;

	}

	public int getActivityIndex() {
		return 0;

	}

	public BaseDocument getAsDocument() {
		Document doc = new Document(env.getDataBase(), Const.sysUser);
		doc.addField("orgname", orgName, FieldType.TEXT);
		doc.addField("orgID", orgID, FieldType.TEXT);
		doc.addField("doccount", 0);
		doc.addField("exptime", "0", FieldType.TEXT);
		doc.addField("liccount", 500);
		doc.addField("sysconfig", "standart", FieldType.TEXT);
		doc.addField("usedlang", "rus", FieldType.TEXT);
		doc.addField("favskin", "classic", FieldType.TEXT);
		doc.addField("activityindex", "100", FieldType.TEXT);
		doc.isValid = true;
		doc.setNewDoc(false);
		return doc;
	}

}
