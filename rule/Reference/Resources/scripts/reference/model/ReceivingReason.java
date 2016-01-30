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
@Table(name = "receiving_reason")
@NamedQuery(name = "ReceivingReason.findAll", query = "SELECT m FROM ReceivingReason AS m ORDER BY m.regDate")
public class ReceivingReason extends Reference {

}
