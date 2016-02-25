package kz.nextbase.script.events;

import kz.flabs.localization.LanguageType;
import kz.flabs.scriptprocessor.page.doscript.AbstractPage;
import kz.lof.scripting._Session;
import kz.nextbase.script._Exception;
import kz.nextbase.script._WebFormData;


public abstract class _DoPage extends AbstractPage {

    @Override
    public void doGET(_Session session, _WebFormData formData, LanguageType lang) throws _Exception {
    }

    @Override
    public void doPOST(_Session session, _WebFormData formData, LanguageType lang) throws _Exception {
    }

    @Override
    public void doDELETE(_Session session, _WebFormData formData, LanguageType lang) throws _Exception {
    }
}
