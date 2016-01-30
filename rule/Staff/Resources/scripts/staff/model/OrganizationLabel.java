package staff.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script._URL;

@Entity
@Table(name = "org_labels")
@NamedQuery(name = "OrganizationLabel.findAll", query = "SELECT m FROM OrganizationLabel AS m ORDER BY m.regDate")
public class OrganizationLabel extends Staff {

	@ManyToMany(mappedBy = "labels")
	private List<Organization> labels;

	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Organization> getLabels() {
		return labels;
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return "<name>" + getName() + "</name><description>" + description + "</description>";
	}

	@Override
	public _URL getURL() {
		return new _URL("Provider?id=organization-label-form&amp;docid=" + getId());
	}

}
