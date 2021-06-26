package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "SELECT * FROM review ORDER BY created_at DESC LIMIT :reviewLimit", nativeQuery = true)
    List<Review> getLatestReviews(int reviewLimit);

    @Query(value = "SELECT * FROM review WHERE id IN (SELECT review_id FROM user_review WHERE user_id = :userId) ORDER BY created_at DESC", nativeQuery = true)
    List<Review> findAllByUserId(long userId);

    @Query(value = "SELECT COUNT(*) FROM review where location_id = :locationId", nativeQuery = true)
    long countAllByLocationId(long locationId);

    @Query(value = "SELECT * FROM review WHERE location_id = :locationId ORDER BY created_at DESC LIMIT 5", nativeQuery = true)
    List<Review> getLatestLocationReviews(long locationId);

    @Query(value = "SELECT user_id FROM user_review WHERE review_id = :reviewId", nativeQuery = true)
    long getUserId(long reviewId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_review WHERE review_id = :reviewId", nativeQuery = true)
    void deleteUserReview(long reviewId);
}
