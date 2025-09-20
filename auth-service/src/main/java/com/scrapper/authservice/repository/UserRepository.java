package com.scrapper.authservice.repository;

import com.scrapper.authservice.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
