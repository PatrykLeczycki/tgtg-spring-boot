package com.pleczycki.tgtg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
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

    @ManyToOne(cascade = CascadeType.ALL)
    private Location location;

    private LocalDateTime pickupTime;

    @Column(nullable = false)
    @NotNull
    private Date createdAt;

    private Date modifiedAt;

//    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
//    private List<Photo> photos;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<Photo> photos;

//    @Exclude
//    @JsonIgnore
//    @ManyToOne(cascade = CascadeType.ALL)
//    private User user;

    private double discountPrice;
    private double standardPrice;
    private int rating;
    private String comment;

}
