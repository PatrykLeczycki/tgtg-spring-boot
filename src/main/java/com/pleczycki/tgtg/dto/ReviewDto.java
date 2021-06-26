package com.pleczycki.tgtg.dto;

import com.pleczycki.tgtg.model.Location;
import lombok.Data;
import lombok.ToString;
import java.util.Date;

@Data
@ToString
public class ReviewDto {

    private Location location;
    private Date pickupTime;

    private double discountPrice;

    private double standardPrice;
    private int rating;
    private String comment;
}
