package com.vladmihalcea.book.hpjp.linhth.association;

import com.vladmihalcea.book.hpjp.util.AbstractTest;
import org.junit.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

public class CompositeIdOneToManyTest extends AbstractTest {
    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Company.class,
                Employee.class
        };
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            Company company = new Company();
            company.setId(1L);
            company.setName("CompanyA");

            Employee employee = new Employee();
            employee.setId(new EmployeeId(company, 1L));
            company.setEmployees(Collections.singletonList(employee));

            entityManager.persist(company);
        });

        doInJPA(entityManager -> {
            Company company = entityManager.find(Company.class, 1L);
            company.getEmployees().get(0).getId().getEmployeeNumber();
            // select employees0_.company_id as company_2_1_0_, employees0_.employee_number as employee1_1_0_, employees0_.company_id as company_2_1_1_, employees0_.employee_number as employee1_1_1_ from employee employees0_ where employees0_.company_id=?
        });
    }


    @Entity(name = "Company")
    @Table(name = "company")
    public static class Company {

        @Id
        private Long id;

        private String name;

        @OneToMany(
            mappedBy="id.company",
            fetch=FetchType.LAZY,
            cascade=CascadeType.ALL
        )
        private List<Employee> employees;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Employee> employees) {
            this.employees = employees;
        }
    }

    @Entity(name = "Employee")
    @Table(name = "employee")
    public static class Employee {

        @EmbeddedId
        private EmployeeId id;

        public EmployeeId getId() {
            return id;
        }

        public void setId(EmployeeId id) {
            this.id = id;
        }
    }

    @Embeddable
    public static class EmployeeId implements Serializable {

        @ManyToOne
        @JoinColumn(name = "company_id",insertable = false, updatable = false)
        private Company company;

        @Column(name = "employee_number")
        private Long employeeNumber;

        public EmployeeId() {
        }

        public EmployeeId(Company company, Long employeeId) {
            this.company = company;
            this.employeeNumber = employeeId;
        }

        public Company getCompany() {
            return company;
        }

        public Long getEmployeeNumber() {
            return employeeNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EmployeeId)) return false;
            EmployeeId that = (EmployeeId) o;
            return Objects.equals(getCompany(), that.getCompany()) &&
                    Objects.equals(getEmployeeNumber(), that.getEmployeeNumber());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCompany(), getEmployeeNumber());
        }
    }
}
