package reference.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import reference.dao.CountryDAO;
import reference.model.Country;
import reference.model.constants.CountryCode;

/**
 * Created by Kayra on 30/12/15.
 */

public class FillCountries extends InitialDataAdapter<Country, CountryDAO> {

	@Override
	public List<Country> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<Country> entities = new ArrayList<Country>();
		String[] data = { "Казахстан", "Россия", "Беларуссия", "Украина", "Германия", "Франция", "Турция", "США", "Китай", "Болгария" };
		String[] dataEng = { "Kazakhstan", "Russia", "Byelorussia", "Ukraine", "Germany", "France", "Turkey", "USA", "China", "Bulgaria" };
		CountryCode[] code = { CountryCode.KZ, CountryCode.RU, CountryCode.BY, CountryCode.UA, CountryCode.DE, CountryCode.FR, CountryCode.TR,
		        CountryCode.US, CountryCode.CN, CountryCode.BG };

		for (int i = 0; i < data.length; i++) {
			Country entity = new Country();
			entity.setName(data[i]);
			Map<LanguageType, String> name = new HashMap<LanguageType, String>();
			name.put(LanguageType.ENG, dataEng[i]);
			name.put(LanguageType.RUS, data[i]);
			entity.setLocalizedName(name);
			entity.setCode(code[i]);
			entities.add(entity);
		}

		return entities;
	}

	@Override
	public Class<CountryDAO> getDAO() {
		return CountryDAO.class;
	}

}
