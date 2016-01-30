package reference.dao;

import java.util.UUID;

import kz.nextbase.script._Session;
import reference.model.BuildingMaterial;

public class BuildingMaterialDAO extends ReferenceDAO<BuildingMaterial, UUID> {

	public BuildingMaterialDAO(_Session session) {
		super(BuildingMaterial.class, session);
	}

}
