package reference.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "building_material")
@NamedQuery(name = "BuildingMaterial.findAll", query = "SELECT m FROM BuildingMaterial AS m ORDER BY m.regDate")
public class BuildingMaterial extends Reference {

}
