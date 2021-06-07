package com.pleczycki.tgtg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@Slf4j
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String street;

    @Column(nullable = false)
    @NotNull
    private String buildingNo;

    @Column(nullable = false)
    @NotNull
    private String city;

    @Column(nullable = false)
    @NotNull
    private double latitude;

    @Column(nullable = false)
    @NotNull
    private double longitude;

    @JsonIgnore
    @Exclude
    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL)
    private final List<Location> locations = new LinkedList<>();

    public Address(Address other) {
        this.setId(other.getId());
        this.setStreet(other.getStreet());
        this.setBuildingNo(other.getBuildingNo());
        this.setCity(other.getCity());
        this.setLatitude(other.getLatitude());
        this.setLongitude(other.getLongitude());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address address = (Address) o;
        return Double.compare(address.latitude, latitude) == 0 &&
                Double.compare(address.longitude, longitude) == 0 &&
                Objects.equals(street, address.street) &&
                Objects.equals(buildingNo, address.buildingNo) &&
                Objects.equals(city, address.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, street, buildingNo, city, latitude, longitude, locations);
    }
}
