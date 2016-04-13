package administrator.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import kz.lof.common.model.SimpleEntity;

@Entity
@Table(name = "_sentences")
@NamedQuery(name = "Sentence.findAll", query = "SELECT m FROM Sentence AS m ORDER BY m.regDate")
public class Sentence extends SimpleEntity {

	private int hits;

}
