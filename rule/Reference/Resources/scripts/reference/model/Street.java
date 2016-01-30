package reference.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "streets")
@NamedQuery(name = "Street.findAll", query = "SELECT m FROM Street AS m ORDER BY m.regDate")
public class Street extends Reference {
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	private Locality locality;

	@Column(name = "street_id")
	private int streetId;

	public int getStreetId() {
		return streetId;
	}

	public void setStreetId(int streetId) {
		this.streetId = streetId;
	}

	public Locality getLocality() {
		return locality;
	}

	public void setLocality(Locality city) {
		this.locality = city;
	}

}
