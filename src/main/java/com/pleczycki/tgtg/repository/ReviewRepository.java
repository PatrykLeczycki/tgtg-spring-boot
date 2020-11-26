package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "SELECT * FROM review ORDER BY created_at DESC LIMIT :reviewLimit", nativeQuery = true)
    List<Review> getLatestReviews(int reviewLimit);

    @Query(value = "SELECT * FROM review WHERE id IN (SELECT review_id FROM user_review WHERE user_id = :userId) ORDER BY created_at DESC", nativeQuery = true)
    List<Review> findAllByUserId(long userId);

    @Query(value = "SELECT * FROM review WHERE location_id = :locationId ORDER BY created_at DESC LIMIT 5", nativeQuery = true)
    List<Review> getLatestLocationReviews(long locationId);
}
