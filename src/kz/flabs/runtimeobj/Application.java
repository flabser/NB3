package kz.flabs.runtimeobj;

import kz.lof.appenv.AppEnv;

public class Application {
	public String orgName = "org_name";
	public String orgID = "org_id";

	private int licenceCount;
	private AppEnv env;

	public Application(AppEnv env) {
		this.env = env;
		// orgName = env.globalSetting.orgName;
		// orgID = env.globalSetting.id;
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

}
