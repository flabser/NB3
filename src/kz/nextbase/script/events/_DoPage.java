package kz.nextbase.script.events;

import kz.flabs.scriptprocessor.page.doscript.AbstractPage;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.nextbase.script._Exception;


public abstract class _DoPage extends AbstractPage {

    @Override
    public void doGET(_Session session, _WebFormData formData) throws _Exception {
    }

    @Override
    public void doPOST(_Session session, _WebFormData formData) throws _Exception {
    }

    @Override
    public void doDELETE(_Session session, _WebFormData formData) throws _Exception {
    }
}
