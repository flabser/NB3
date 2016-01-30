package reference.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import reference.dao.TagDAO;
import reference.model.Tag;

/**
 * Created by Kayra on 28/01/16.
 */

public class FillTags extends InitialDataAdapter<Tag, TagDAO> {

	@Override
	public List<Tag> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<Tag> entities = new ArrayList<Tag>();

		Tag entity = new Tag();
		entity.setName("on_balance");
		Map<LanguageType, String> name = new HashMap<LanguageType, String>();
		name.put(LanguageType.ENG, "On the balance");
		name.put(LanguageType.RUS, "На балансе");
		name.put(LanguageType.KAZ, "На балансе");
		entity.setLocalizedName(name);
		entities.add(entity);

		entity = new Tag();
		entity.setName("written-off");
		name = new HashMap<LanguageType, String>();
		name.put(LanguageType.ENG, "Written-off");
		name.put(LanguageType.RUS, "Списан");
		name.put(LanguageType.KAZ, "Списан");
		entity.setLocalizedName(name);
		entities.add(entity);

		return entities;
	}

	@Override
	public Class<TagDAO> getDAO() {
		return TagDAO.class;
	}

}
