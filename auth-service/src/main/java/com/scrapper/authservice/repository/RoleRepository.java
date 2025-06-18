package com.scrapper.authservice.repository;

import com.scrapper.authservice.entity.Role;
import com.scrapper.authservice.user.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(RoleType role);
    boolean existsByRole(RoleType role);
}
