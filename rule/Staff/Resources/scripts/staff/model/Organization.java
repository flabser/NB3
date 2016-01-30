package staff.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "orgs")
@NamedQuery(name = "Organization.findAll", query = "SELECT m FROM Organization AS m ORDER BY m.regDate")
public class Organization extends Staff {
	@OneToMany(mappedBy = "organization")
	private List<Department> departments;

	@OneToMany(mappedBy = "organization")
	private List<Employee> employers;

	@Column(name = "is_primary")
	private boolean isPrimary;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "org_org_label")
	private List<OrganizationLabel> labels;

	public List<Department> getDepartments() {
		return departments;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public List<Employee> getEmployers() {
		return employers;
	}

	public void setEmployers(List<Employee> employers) {
		this.employers = employers;
	}

}
