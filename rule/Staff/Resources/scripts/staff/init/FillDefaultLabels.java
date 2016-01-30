package staff.init;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import staff.dao.OrganizationLabelDAO;
import staff.model.OrganizationLabel;

/**
 * 
 * 
 * @author Kayra created 09-01-2016
 */

public class FillDefaultLabels extends InitialDataAdapter<OrganizationLabel, OrganizationLabelDAO> {

	@Override
	public List<OrganizationLabel> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<OrganizationLabel> entities = new ArrayList<OrganizationLabel>();

		OrganizationLabel entity = new OrganizationLabel();
		entity.setName("inactive");
		entity.setDescription("Inactive organization");
		entities.add(entity);

		entity = new OrganizationLabel();
		entity.setName("branch");
		entity.setDescription("organization is a branch of the primary organization");
		entities.add(entity);

		/* ComProperty application specific labels */
		entity = new OrganizationLabel();
		entity.setName("balance_holder");
		entity.setDescription("Организация-балансодержатель");
		entities.add(entity);

		return entities;
	}

	@Override
	public Class<OrganizationLabelDAO> getDAO() {
		return OrganizationLabelDAO.class;
	}

}
