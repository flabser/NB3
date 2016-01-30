package staff.dao;

import java.util.UUID;

import kz.flabs.dataengine.jpa.DAO;
import kz.nextbase.script._Session;
import staff.model.OrganizationLabel;

public class OrganizationLabelDAO extends DAO<OrganizationLabel, UUID> {

	public OrganizationLabelDAO(_Session session) {
		super(OrganizationLabel.class, session);
	}

}
