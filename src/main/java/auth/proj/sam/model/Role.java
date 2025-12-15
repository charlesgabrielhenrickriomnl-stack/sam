package auth.proj.sam.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    // ADD THIS MAPPING
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // ADD GETTER AND SETTER FOR USERS
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}