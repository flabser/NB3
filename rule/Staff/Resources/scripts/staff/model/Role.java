package staff.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import kz.flabs.localization.LanguageType;

@Entity
@Table(name = "roles")
@NamedQuery(name = "Role.findAll", query = "SELECT m FROM Role AS m ORDER BY m.regDate")
public class Role extends Staff {

	@ManyToMany(mappedBy = "roles")
	private List<Employee> employees;

	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return "<name>" + getName() + "</name><description>" + description + "</description>";
	}
}
