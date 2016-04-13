package administrator.page.form;

import kz.flabs.util.Util;
import kz.lof.common.page.form.Form;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;

public class ServerForm extends Form {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		addValue("hostname", Environment.hostName);
		addValue("port", Environment.httpPort);
		addValue("tmpdir", Environment.tmpDir);
		addValue("orgname", Environment.orgName);
		addValue("database", Environment.dataBase.getInfo());
		addValue("devmode", Environment.isDevMode());
		addValue("officeframe", Environment.getOfficeFrameDir());
		addValue("kernel", Environment.getKernelDir());
		addValue("starttime", Util.convertDataTimeToString(Environment.startTime));
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData) {
		devPrint(formData);

	}

}
