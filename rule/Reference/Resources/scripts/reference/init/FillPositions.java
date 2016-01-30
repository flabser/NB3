package reference.init;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import reference.dao.PositionDAO;
import reference.model.Position;

/**
 * Created by Kayra on 30/12/15.
 */

public class FillPositions extends InitialDataAdapter<Position, PositionDAO> {

	@Override
	public List<Position> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<Position> entities = new ArrayList<Position>();
		String[] data = { "Директор", "Менеджер", "Бухгалтер", "Инженер", "Специалист", "Секретарь-референт", "Администратор",
		        "Руководитель подразделения", "Экспедитор" };

		for (int i = 0; i < data.length; i++) {
			Position entity = new Position();
			entity.setName(data[i]);
			entities.add(entity);
		}

		return entities;
	}

	@Override
	public Class<PositionDAO> getDAO() {
		return PositionDAO.class;
	}

}
