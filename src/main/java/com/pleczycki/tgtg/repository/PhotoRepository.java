package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {

}
