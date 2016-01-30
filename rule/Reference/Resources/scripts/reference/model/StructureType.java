package reference.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "structure_type")
@NamedQuery(name = "StructureType.findAll", query = "SELECT m FROM StructureType AS m ORDER BY m.regDate")
public class StructureType extends Reference {

}
