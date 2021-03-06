package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = "SELECT AVG(review.rating) FROM review "
            + "WHERE review.location_id = :location_id", nativeQuery = true)
    double getAverageRating(@Param("location_id") Long locationId);

    @Query(value = "SELECT * FROM location loc "
            + "JOIN address a ON a.id = loc.address_id "
            + "WHERE a.city LIKE :city ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    List<Location> getLatestLocationsByCity(@Param("city") String city);

    @Query(value = "SELECT * FROM location WHERE id IN (SELECT location_id FROM user_location WHERE user_id = :userId) ORDER BY created_at DESC", nativeQuery = true)
    List<Location> findAllByUserId(long userId);

    @Query(value = "SELECT * FROM location loc WHERE loc.id IN (SELECT DISTINCT location_id FROM user_blacklist)", nativeQuery = true)
    List<Location> getDistinctBlacklist();

    @Query(value = "SELECT * FROM location loc WHERE loc.id IN (SELECT location_id FROM user_blacklist)", nativeQuery = true)
    List<Location> getBlacklist();

    @Query(value = "SELECT COUNT(*) FROM user_blacklist WHERE location_id = :locationId", nativeQuery = true)
    int countLocationOnBlacklist(@Param("locationId") Long locationId);

    @Query(value = "SELECT user_id FROM user_location WHERE location_id = :locationId", nativeQuery = true)
    long getLocationUserId(long locationId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_location WHERE location_id = :locationId", nativeQuery = true)
    void deleteUserLocation(long locationId);
}
