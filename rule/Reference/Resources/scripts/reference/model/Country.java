package reference.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import reference.model.constants.CountryCode;

@Entity
@Table(name = "countries")
@NamedQuery(name = "Country.findAll", query = "SELECT m FROM Country AS m ORDER BY m.regDate")
public class Country extends Reference {
	private List<Region> regions;

	@Enumerated(EnumType.STRING)
	@Column(name = "country_code", nullable = true, length = 7, unique = true)
	private CountryCode code = CountryCode.UNKNOWN;

	public CountryCode getCode() {
		return code;
	}

	public void setCode(CountryCode code) {
		this.code = code;
	}

	@OneToMany(mappedBy = "country")
	public List<Region> getRegions() {
		return regions;
	}

	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

}
