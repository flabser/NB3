package reference.init;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import reference.dao.DistrictDAO;
import reference.dao.RegionDAO;
import reference.model.District;
import reference.model.Region;

/**
 * Created by Kayra on 30/12/15.
 */

public class FillDistricts extends InitialDataAdapter<District, DistrictDAO> {

	@Override
	public List<District> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {

		List<District> entities = new ArrayList<District>();
		String[] data = { "Алатауский", "Алмалинский", "Ауэзовский", "Бостандыкский", "Жетысуский", "Медеуский", "Наурызбайский", "Турксибский" };

		RegionDAO cDao = new RegionDAO(ses);
		Region region = cDao.findByName("Алматы");

		for (int i = 0; i < data.length; i++) {
			District entity = new District();
			entity.setRegion(region);
			entity.setName(data[i]);
			entities.add(entity);
		}

		return entities;

	}

	@Override
	public Class<DistrictDAO> getDAO() {
		return DistrictDAO.class;
	}

}
