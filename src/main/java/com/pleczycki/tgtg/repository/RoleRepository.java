package com.pleczycki.tgtg.repository;

import com.pleczycki.tgtg.model.Role;
import com.pleczycki.tgtg.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
}