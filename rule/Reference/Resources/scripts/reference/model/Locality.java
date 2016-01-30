package reference.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import reference.model.constants.LocalityType;

@Entity
@Table(name = "localities")
@NamedQuery(name = "Locality.findAll", query = "SELECT m FROM Locality AS m ORDER BY m.regDate")
public class Locality extends Reference {
	private List<Street> streets;

	@Enumerated(EnumType.STRING)
	@Column(name = "locality_type", nullable = true, length = 16)
	private LocalityType type = LocalityType.UNKNOWN;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	private District district;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	private Region region;

	@OneToMany(mappedBy = "locality")
	public List<Street> getStreets() {
		return streets;
	}

	public LocalityType getType() {
		return type;
	}

	public void setType(LocalityType type) {
		this.type = type;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public void setStreets(List<Street> streets) {
		this.streets = streets;
	}

	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

}
