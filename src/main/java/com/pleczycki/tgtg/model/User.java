package com.pleczycki.tgtg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@NoArgsConstructor
@Data
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    private boolean enabled;

    private String registrationToken;

    private String passRecoveryToken;

    @Column(nullable = false)
    @NotNull
    private Date createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    //    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @OneToMany
    @JoinTable(name = "user_review", joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "review_id"))
    private List<Review> reviews = new LinkedList<>();

    @Exclude
//    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @ManyToMany
    @JoinTable(name = "user_blacklist", joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<Location> locationsBlacklist = new LinkedList<>();

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(User other) {
        this.setId(other.getId());
        this.setReviews(other.getReviews());
        this.setUsername(other.getUsername());
        this.setCreatedAt(other.getCreatedAt());
        this.setPassRecoveryToken(other.getPassRecoveryToken());
        this.setRegistrationToken(other.getRegistrationToken());
        this.setPassword(other.getPassword());
        this.setEnabled(other.isEnabled());
        this.setEmail(other.getEmail());
        this.setRoles(other.getRoles());
        this.setLocationsBlacklist(other.getLocationsBlacklist());
    }
}