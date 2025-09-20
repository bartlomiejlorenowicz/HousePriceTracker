package com.scrapper.repository;

import com.scrapper.entity.UserApartmentWatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserApartmentWatchRepository extends JpaRepository<UserApartmentWatch, Long> {

    Optional<UserApartmentWatch> findByUserIdAndApartment_Id(Long userId, Long apartmentId);

    List<UserApartmentWatch> findAllByUserIdAndActive(Long userId, boolean active);

    List<UserApartmentWatch> findAllByApartment_IdAndActiveTrue(Long apartmentId);
}
