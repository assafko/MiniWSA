package com.miniwsa.repository;

import com.miniwsa.domain.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    @Query(value = "SELECT se FROM SecurityEvent se WHERE se.clientIp = :clientIp " +
                   "AND se.receivedAt >= :tenMinutesAgo ORDER BY se.receivedAt DESC")
    List<SecurityEvent> findRecentEventsByClientIp(@Param("clientIp") String clientIp,
                                                     @Param("tenMinutesAgo") Long tenMinutesAgo);
}

