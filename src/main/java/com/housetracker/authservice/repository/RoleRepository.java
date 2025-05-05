package com.housetracker.authservice.repository;

import com.housetracker.authservice.entity.Role;
import com.housetracker.authservice.user.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(RoleType role);
    boolean existsByRole(RoleType role);
}
