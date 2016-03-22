package kz.lof.administrator.page.navigator;

import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.lof.scriptprocessor.page.IOutcomeObject;
import kz.nextbase.script.outline._Outline;
import kz.nextbase.script.outline._OutlineEntry;

import java.util.ArrayList;
import java.util.List;


public class MainNavigator extends _DoPage {

    @Override
    public void doGET(_Session session, _WebFormData formData) {
        LanguageCode lang = session.getLang();
        List<IOutcomeObject> list = new ArrayList<IOutcomeObject>();

        _Outline common_outline = new _Outline(getLocalizedWord("administrator", lang), "common");
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("user", lang), "user-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("application", lang), "application-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("language", lang), "language-view"));

        list.add(common_outline);

        addContent("outline_current", formData.getValueSilently("id").replace("-form", "-view"));
        addContent(list);
    }
}
