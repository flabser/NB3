package reference.dao;

import java.util.UUID;

import kz.nextbase.script._Session;
import reference.model.District;

public class DistrictDAO extends ReferenceDAO<District, UUID> {

	public DistrictDAO(_Session session) {
		super(District.class, session);
	}

}
