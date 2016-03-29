package kz.lof.common.page.view;

import java.util.UUID;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.lof.dataengine.jpa.AppEntity;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;

/**
 * @author Kayra created 26-03-2016
 */

public class FTSearch extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		LanguageCode lang = session.getLang();
		String keyWord = formData.getValueSilently("keyword");
		if (keyWord.isEmpty()) {
			setBadRequest();
			return;
		}
		int pageNum = formData.getNumberValueSilently("page", 1);
		int pageSize = session.pageSize;

		IDatabase db = session.getDatabase();
		IFTIndexEngine ftEngine = db.getFTSearchEngine();
		ViewPage<?> result = ftEngine.search(keyWord, session, pageNum, pageSize);

		addContent(new _ActionBar(session).addAction(new _Action(getLocalizedWord("back_to_doc_list", lang), getLocalizedWord("back", lang),
		        "reset_search")));
		if (result != null) {
			ViewPage<AppEntity<UUID>> res = (ViewPage<AppEntity<UUID>>) result;
			addContent(new _POJOListWrapper<>(res.getResult(), res.getMaxPage(), res.getCount(), res.getPageNum(), session, keyWord));
		} else {
			addContent(new _POJOListWrapper(getLocalizedWord("ft_search_resturn_null", lang) + ": '" + keyWord + "'", keyWord));
		}
		addValue("request_param", "keyword=" + keyWord);
	}
}
