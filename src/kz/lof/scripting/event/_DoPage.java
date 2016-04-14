package kz.lof.scripting.event;

import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scriptprocessor.page.AbstractPage;
import kz.nextbase.script._Exception;

public abstract class _DoPage extends AbstractPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) throws _Exception {
	}

	@Override
	public void doPUT(_Session session, _WebFormData formData) throws _Exception {
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData) throws _Exception {
	}

	@Override
	public void doDELETE(_Session session, _WebFormData formData) throws _Exception {
	}
}
