package staff.model;

import staff.model.constants.DepartmentType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;


@Entity
@Table(name = "departments")
@NamedQuery(name = "Department.findAll", query = "SELECT m FROM Department AS m ORDER BY m.regDate")
public class Department extends Staff {
    private DepartmentType type;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Organization organization;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public DepartmentType getType() {
        return type;
    }

    public void setType(DepartmentType type) {
        this.type = type;
    }
}
