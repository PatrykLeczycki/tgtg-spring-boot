package com.pleczycki.tgtg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Location location;

    private Date pickupTime;

    @Column(nullable = false)
    @NotNull
    private Date createdAt;

    private Date modifiedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<Photo> photos = new LinkedList<>();

    private double discountPrice;
    private double standardPrice;
    private int rating;
    private String comment;

    public Review(Review other) {
        this.setId(other.getId());
        this.setLocation(other.getLocation());
        this.setModifiedAt(other.getModifiedAt());
        this.setDiscountPrice(other.getDiscountPrice());
        this.setStandardPrice(other.getStandardPrice());
        this.setRating(other.getRating());
        this.setComment(other.getComment());
        this.setCreatedAt(other.getCreatedAt());
        this.setPickupTime(other.getPickupTime());
        this.setPhotos(other.getPhotos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Review review = (Review) o;
        return Double.compare(review.discountPrice, discountPrice) == 0 &&
                Double.compare(review.standardPrice, standardPrice) == 0 &&
                rating == review.rating &&
                Objects.equals(id, review.id) &&
                Objects.equals(location, review.location) &&
                Objects.equals(pickupTime, review.pickupTime) &&
                Objects.equals(createdAt, review.createdAt) &&
                Objects.equals(modifiedAt, review.modifiedAt) &&
                Objects.equals(photos, review.photos) &&
                Objects.equals(comment, review.comment);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(id, location, pickupTime, createdAt, modifiedAt, photos, discountPrice, standardPrice, rating,
                        comment);
    }
}
