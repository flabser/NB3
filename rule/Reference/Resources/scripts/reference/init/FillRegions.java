package reference.init;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import reference.dao.CountryDAO;
import reference.dao.RegionDAO;
import reference.model.Country;
import reference.model.Region;
import reference.model.constants.RegionType;

/**
 * Created by Kayra on 30/12/15.
 */

public class FillRegions extends InitialDataAdapter<Region, RegionDAO> {

	@Override
	public List<Region> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<Region> entities = new ArrayList<Region>();
		String[] data = { "Алматы", "Астана", "Алматинская", "Акмолинская", "Джамбульская", "Мангистауская", "ЮКО", "ВКО" };

		CountryDAO cDao = new CountryDAO(ses);
		Country country = cDao.findByName("Казахстан");

		for (int i = 0; i < data.length; i++) {
			Region entity = new Region();
			entity.setCountry(country);
			entity.setName(data[i]);
			if (data.equals("Алматы") || data.equals("Астана")) {
				entity.setType(RegionType.URBAN_AGGLOMERATION);
			} else {
				entity.setType(RegionType.REGION);
			}
			entities.add(entity);
		}
		return entities;

	}

	@Override
	public Class<RegionDAO> getDAO() {
		return RegionDAO.class;
	}

}
