package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query(value = "SELECT DISTINCT city FROM address" , nativeQuery = true)
    List<String> getDistinctCities();
}
