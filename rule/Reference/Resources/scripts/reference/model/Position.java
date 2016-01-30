package reference.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * 
 * 
 * @author Kayra created 07-01-2016
 */

@Entity
@Table(name = "positions")
@NamedQuery(name = "Position.findAll", query = "SELECT m FROM Position AS m ORDER BY m.regDate")
public class Position extends Reference {

}
