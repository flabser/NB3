package administrator.page.navigator;

import java.util.ArrayList;
import java.util.List;

import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.lof.scriptprocessor.page.IOutcomeObject;
import kz.nextbase.script.outline._Outline;
import kz.nextbase.script.outline._OutlineEntry;

public class MainNavigator extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		LanguageCode lang = session.getLang();
		List<IOutcomeObject> list = new ArrayList<IOutcomeObject>();

		_Outline common_outline = new _Outline(getLocalizedWord("Administrator", lang), "common");
		common_outline.addEntry(new _OutlineEntry("Server", "", "server", "p?id=server-form"));
		common_outline.addEntry(new _OutlineEntry("User", "user-view"));
		common_outline.addEntry(new _OutlineEntry("Applications", "application-view"));
		common_outline.addEntry(new _OutlineEntry("Languages", "language-view"));
		// common_outline.addEntry(new _OutlineEntry("Sentences",
		// "sentence-view"));
		common_outline.addEntry(new _OutlineEntry("Log", "log-view"));

		list.add(common_outline);

		addContent("outline_current", formData.getValueSilently("id").replace("-form", "-view"));
		addContent(list);
	}
}
