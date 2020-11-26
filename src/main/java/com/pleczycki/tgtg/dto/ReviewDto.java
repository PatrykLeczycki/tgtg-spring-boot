package com.pleczycki.tgtg.dto;

import com.pleczycki.tgtg.model.Location;
//import com.pleczycki.tgtg.model.Photo;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Data
@ToString
public class ReviewDto {

    private Location location;
    private LocalDateTime pickupTime;

    private double discountPrice;

    private double standardPrice;
    private int rating;
    private String comment;
}
