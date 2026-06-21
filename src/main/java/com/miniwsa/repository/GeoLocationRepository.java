package com.miniwsa.repository;

import com.miniwsa.domain.entity.GeoLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeoLocationRepository extends JpaRepository<GeoLocation, Long> {
    Optional<GeoLocation> findByIpAddress(String ipAddress);
}

