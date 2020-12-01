package com.pleczycki.tgtg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@Slf4j
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @NotNull
    private Address address;

    @Column(nullable = false)
    @NotNull
    private double rating;

    @Column(nullable = false)
    @NotNull
    private Date createdAt;

    private Date modifiedAt;

    @Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private final List<Review> reviews = new LinkedList<>();

    public Location(Location other) {
        this.setId(other.getId());
        this.setName(other.getName());
        this.setModifiedAt(other.getModifiedAt());
        this.setCreatedAt(other.getCreatedAt());
        this.setRating(other.getRating());
        this.setAddress(other.getAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        return Double.compare(location.rating, rating) == 0 &&
                Objects.equals(id, location.id) &&
                Objects.equals(name, location.name) &&
                Objects.equals(address, location.address) &&
                Objects.equals(createdAt, location.createdAt) &&
                Objects.equals(modifiedAt, location.modifiedAt) &&
                Objects.equals(reviews, location.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, rating, createdAt, modifiedAt, reviews);
    }
}
