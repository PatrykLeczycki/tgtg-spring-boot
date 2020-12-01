package com.pleczycki.tgtg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {
    private static Review review;

    @BeforeEach
    void setup() {
        review = initializeReview();
    }

    private static Review initializeReview() {
        Review review = new Review();

        Location location = new Location();
        Address tempAddress = new Address();

        tempAddress.setId(1L);
        tempAddress.setStreet("Street");
        tempAddress.setBuildingNo("123");
        tempAddress.setCity("City");
        tempAddress.setLatitude(52.2347399);
        tempAddress.setLongitude(21.0094018);

        location.setId(1L);
        location.setAddress(tempAddress);
        location.setName("locationName");
        location.setRating(5.0);

        review.setId(1L);
        review.setLocation(location);
        review.setPhotos(new LinkedList<>());
        review.setPickupTime(new Date());
        review.setCreatedAt(new Date());
        review.setModifiedAt(new Date());
        review.setStandardPrice(30);
        review.setDiscountPrice(10);
        review.setRating(5);
        review.setComment("comment");

        return review;
    }

    @Test
    void clonedReviewShouldBeEqual() {
        //given
        Review newReview = new Review(review);

        //when
        //then
        assertEquals(review, newReview);
    }
}