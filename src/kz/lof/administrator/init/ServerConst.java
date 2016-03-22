package kz.lof.administrator.init;

import java.util.HashMap;
import java.util.Map;

import kz.lof.administrator.model.Language;
import kz.lof.env.Environment;
import kz.lof.localization.LanguageCode;

public class ServerConst {

	public static Language getData(LanguageCode code) {
		Map<LanguageCode, String[]> lName = new HashMap<LanguageCode, String[]>();
		String[] dataEng = { "English", "Kazakh", "Russian" };
		String[] dataKaz = { "Ағылшын", "Қазақша", "Орысша" };
		String[] dataRus = { "Английски", "Казахский", "Русский" };
		lName.put(LanguageCode.ENG, dataEng);
		lName.put(LanguageCode.KAZ, dataKaz);
		lName.put(LanguageCode.RUS, dataRus);

		Language entity = new Language();
		entity.setName(code.toString());
		Map<LanguageCode, String> name = new HashMap<LanguageCode, String>();
		for (LanguageCode lc1 : Environment.langs) {
			// name.put(lc1, lName.get(lc1));
		}
		entity.setLocalizedName(name);
		entity.setCode(code);

		return entity;
	}
}
