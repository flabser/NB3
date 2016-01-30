package reference.init;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import reference.dao.PropertyCodeDAO;
import reference.model.PropertyCode;

/**
 * Created by Kayra on 30/12/15.
 */

public class FillPropertyCode extends InitialDataAdapter<PropertyCode, PropertyCodeDAO> {

	@Override
	public List<PropertyCode> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<PropertyCode> entities = new ArrayList<PropertyCode>();
		String[] data = { "Безвозмездное пользование", "Временное безвоздмездное пользование", "Временное безвозмездное пользование",
		        "Доверительное управление", "Долгосрочная аренда", "Концессия", "Оперативное управление", "Постановление о создании", "Распоряжение",
		        "Собственность", "Хозяйственное ведение" };

		for (int i = 0; i < data.length; i++) {
			PropertyCode entity = new PropertyCode();
			entity.setName(data[i]);
			entities.add(entity);
		}

		return entities;
	}

	@Override
	public Class<PropertyCodeDAO> getDAO() {
		return PropertyCodeDAO.class;
	}

}
