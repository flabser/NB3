package kz.lof.administrator.page.form;

import kz.lof.administrator.dao.LanguageDAO;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script._AppEntourage;
import kz.nextbase.script._Exception;


public class LoginForm extends _DoPage {
    @Override
    public void doGET(_Session session, _WebFormData formData) throws _Exception {
        _AppEntourage ent = session.getAppEntourage();
        addValue("serverversion", ent.getServerVersion());
        addValue("build", ent.getBuildTime());

        String lang = formData.getValueSilently("lang");
        if (!lang.isEmpty()) {
            session.setLang(LanguageCode.valueOf(lang));
        }

        addContent(new _POJOListWrapper(new LanguageDAO(session).findAll(), session));
    }
}
