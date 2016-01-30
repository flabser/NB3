package reference.dao;

import java.util.UUID;

import kz.nextbase.script._Session;
import reference.model.PropertyCode;

public class PropertyCodeDAO extends ReferenceDAO<PropertyCode, UUID> {

	public PropertyCodeDAO(_Session session) {
		super(PropertyCode.class, session);
	}

}
