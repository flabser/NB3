package kz.lof.administrator.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.flabs.localization.Vocabulary;
import kz.lof.administrator.dao.LanguageDAO;
import kz.lof.administrator.model.Language;
import kz.lof.dataengine.jpa.deploying.InitialDataAdapter;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;

/**
 * @author Created by Kayra on 27/02/16.
 */

// TODO it is temporary thing
public class FillLangs extends InitialDataAdapter<Language, LanguageDAO> {

	@Override
	public List<Language> getData(_Session ses, LanguageCode lang, Vocabulary vocabulary) {
		List<Language> entities = new ArrayList<Language>();
		String[] dataEng = { "English", "Kazakh", "Russian" };
		String[] dataKaz = { "Ағылшын", "Қазақша", "Орысша" };
		String[] dataRus = { "Английски", "Казахский", "Русский" };

		LanguageCode[] code = { LanguageCode.ENG, LanguageCode.KAZ, LanguageCode.RUS };

		for (int i = 0; i < code.length; i++) {
			Language entity = new Language();
			entity.setName(code[i].toString());
			Map<LanguageCode, String> name = new HashMap<LanguageCode, String>();
			name.put(LanguageCode.ENG, dataEng[i]);
			name.put(LanguageCode.KAZ, dataKaz[i]);
			name.put(LanguageCode.RUS, dataRus[i]);
			entity.setLocalizedName(name);
			entity.setCode(code[i]);
			entities.add(entity);
		}

		return entities;
	}

	@Override
	public Class<LanguageDAO> getDAO() {
		return LanguageDAO.class;
	}

}
