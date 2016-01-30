package reference.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "subdivisions")
@NamedQuery(name = "Subdivision.findAll", query = "SELECT m FROM Subdivision AS m ORDER BY m.regDate")
public class Subdivision extends Reference {

}
