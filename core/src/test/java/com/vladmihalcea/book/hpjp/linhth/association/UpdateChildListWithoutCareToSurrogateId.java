package com.vladmihalcea.book.hpjp.linhth.association;

import com.vladmihalcea.book.hpjp.util.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UpdateChildListWithoutCareToSurrogateId extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Team.class,
                EmployeeRelation.class
        };
    }

    @Test
    public void testInsert() {
        doInJPA(entityManager -> {
            Team team = new Team();
            team.setName("Neon");

            EmployeeRelation e1 = new EmployeeRelation();
            e1.setEmployeeId(1);
            e1.setRole("Scrum");

            EmployeeRelation e2 = new EmployeeRelation();
            e2.setEmployeeId(2);
            e2.setRole("Dev");

            EmployeeRelation e3 = new EmployeeRelation();
            e3.setEmployeeId(3);
            e3.setRole("Test");

            team.setEmployeeRelations(Arrays.asList(e1, e2, e3));

            entityManager.persist(team);
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);

            Assert.assertEquals(3, team.getEmployeeRelations().size());
        });
    }

    @Test
    public void testAdd() {
        doInJPA(entityManager -> {
            Team team = new Team();
            team.setName("Neon");

            EmployeeRelation e1 = new EmployeeRelation();
            e1.setEmployeeId(1);
            e1.setRole("Scrum");

            EmployeeRelation e2 = new EmployeeRelation();
            e2.setEmployeeId(2);
            e2.setRole("Dev");

            team.setEmployeeRelations(Arrays.asList(e1, e2));

            entityManager.persist(team);
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);
            EmployeeRelation e3 = new EmployeeRelation();
            e3.setEmployeeId(3);
            e3.setRole("Test");

            team.addEmployeeRelation(e3);
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);

            Assert.assertEquals(3, team.getEmployeeRelations().size());
        });
    }

    @Test
    public void testRemove() {
        doInJPA(entityManager -> {
            Team team = new Team();
            team.setName("Neon");

            EmployeeRelation e1 = new EmployeeRelation();
            e1.setEmployeeId(1);
            e1.setRole("Scrum");

            EmployeeRelation e2 = new EmployeeRelation();
            e2.setEmployeeId(2);
            e2.setRole("Dev");

            EmployeeRelation e3 = new EmployeeRelation();
            e3.setEmployeeId(3);
            e3.setTeam(team);
            e3.setRole("Test");

            team.setEmployeeRelations(Arrays.asList(e1, e2, e3));

            entityManager.persist(team);
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);
            EmployeeRelation e3 = new EmployeeRelation();
            e3.setEmployeeId(3);
            e3.setRole("Test");
            e3.setTeam(team);

            Optional<EmployeeRelation> employeeRelation = team.getEmployeeRelations().stream().filter(e3::equals).findFirst();

            team.removeEmployeeRelation(employeeRelation.get());
            entityManager.remove(employeeRelation.get());
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);

            Assert.assertEquals(2, team.getEmployeeRelations().size());
            Assert.assertNull(entityManager.find(EmployeeRelation.class, 4L));
        });
    }

    @Test
    public void testUpdate() {
        doInJPA(entityManager -> {
            Team team = new Team();
            team.setName("Neon");

            EmployeeRelation e1 = new EmployeeRelation();
            e1.setEmployeeId(1);
            e1.setRole("Scrum");

            EmployeeRelation e2 = new EmployeeRelation();
            e2.setEmployeeId(2);
            e2.setRole("Dev");

            EmployeeRelation e3 = new EmployeeRelation();
            e3.setEmployeeId(3);
            e3.setTeam(team);
            e3.setRole("Test");

            team.setEmployeeRelations(Arrays.asList(e1, e2, e3));

            entityManager.persist(team);
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);

            // Given
            EmployeeRelation e2 = new EmployeeRelation();
            e2.setEmployeeId(2);
            e2.setRole("Dev");

            EmployeeRelation e4 = new EmployeeRelation();
            e4.setEmployeeId(4);
            e4.setRole("Scrum");

            List<EmployeeRelation> updateRelations = Arrays.asList(e2, e4);
            updateRelations.forEach(u -> u.setTeam(team));

            List<EmployeeRelation> olderRelations = team.getEmployeeRelations();

            olderRelations.stream()
                    .filter(not(updateRelations::contains))
                    .collect(Collectors.toList())
                    .forEach(team::removeEmployeeRelation);

            updateRelations.stream()
                    .filter(not(olderRelations::contains))
                    .collect(Collectors.toList())
                    .forEach(team::addEmployeeRelation);
        });

        doInJPA(entityManager -> {
            Team team = entityManager.find(Team.class, 1L);

            Assert.assertEquals(2, team.getEmployeeRelations().size());
            Assert.assertEquals(2L, team.getEmployeeRelations().get(0).getEmployeeId());
            Assert.assertEquals(4L, team.getEmployeeRelations().get(1).getEmployeeId());
            Assert.assertNull(entityManager.find(EmployeeRelation.class, 2L));
            Assert.assertNull(entityManager.find(EmployeeRelation.class, 4L));
        });
    }

    @Entity(name = "Team")
    @Table(name = "team")
    public static class Team {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @OneToMany(
                mappedBy="team",
                cascade=CascadeType.ALL,
                orphanRemoval = true
        )
        private List<EmployeeRelation> employeeRelations = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<EmployeeRelation> getEmployeeRelations() {
            return employeeRelations;
        }

        public void setEmployeeRelations(List<EmployeeRelation> employeeRelations) {
            employeeRelations.forEach(employeeRelation -> employeeRelation.setTeam(this));
            this.employeeRelations = employeeRelations;
        }

        public void addEmployeeRelation(EmployeeRelation employeeRelation) {
            employeeRelation.setTeam(this);
            this.employeeRelations.add(employeeRelation);
        }

        public void removeEmployeeRelation(EmployeeRelation employeeRelation) {
            employeeRelations.remove(employeeRelation);
            employeeRelation.setTeam(null);
        }
    }

    @Entity(name = "EmployeeRelation")
    @Table(name = "employeeRelation")
    public static class EmployeeRelation {

        @Id
        @GeneratedValue
        private long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "team_id")
        private Team team;

        private long employeeId;

        private String role;

        public Team getTeam() {
            return team;
        }

        public void setTeam(Team team) {
            this.team = team;
        }

        public long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(long employeeId) {
            this.employeeId = employeeId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EmployeeRelation)) return false;
            EmployeeRelation that = (EmployeeRelation) o;
            return Objects.equals(getTeam(), that.getTeam()) &&
                    Objects.equals(getEmployeeId(), that.getEmployeeId()) &&
                    Objects.equals(getRole(), that.getRole());
        }

        @Override
        public int hashCode() { return Objects.hash(getTeam(), getEmployeeId(), getRole()); }
    }

    private static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }
}
