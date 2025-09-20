package com.scrapper.authservice.repository;

import com.scrapper.authservice.entity.Role;
import com.scrapper.authservice.user.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(RoleType role);

    @Query(value = "select r.id from Role r where r.role = :role", nativeQuery = true)
    Optional<Long> findIdByRole(@Param("role") RoleType role);

    boolean existsByRole(RoleType role);
}
