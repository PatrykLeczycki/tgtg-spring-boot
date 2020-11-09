package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);
}