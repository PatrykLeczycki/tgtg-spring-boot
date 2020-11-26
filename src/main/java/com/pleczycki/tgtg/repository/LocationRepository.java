package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.BlacklistLocationDto;
import com.pleczycki.tgtg.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = "SELECT AVG(review.rating) FROM review "
            + "WHERE review.location_id = :location_id", nativeQuery = true)
    double getAverageRating(@Param("location_id") Long locationId);

    @Query(value = "SELECT * FROM location loc "
            + "JOIN address a ON a.id = loc.address_id "
            + "WHERE a.city LIKE :city", nativeQuery = true)
    List<Location> getAllByCity(@Param("city") String city);

    @Query(value = "SELECT * FROM location loc "
            + "JOIN address a ON a.id = loc.address_id "
            + "WHERE a.city LIKE :city ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    List<Location> getLatestLocationsByCity(@Param("city") String city);

    @Query(value = "INSERT INTO user_blacklist (user_id, location_id) VALUES (:userId, :locationId)" , nativeQuery = true)
    void addToBlacklist(@Param("userId") Long userId, @Param("locationId") Long locationId);

    @Query(value = "SELECT * FROM location loc WHERE loc.id IN (SELECT DISTINCT location_id FROM user_blacklist)", nativeQuery = true)
    List<Location> getBlacklist();

    @Query(value = "SELECT COUNT(*) FROM user_blacklist WHERE location_id = :locationId", nativeQuery = true)
    int countLocationOnBlacklist(@Param("locationId") Long locationId);
}
