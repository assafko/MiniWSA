package com.miniwsa.repository;

import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.repository.projection.ActionCountProjection;
import com.miniwsa.repository.projection.CategoryStatsProjection;
import com.miniwsa.repository.projection.TopAttackerProjection;
import com.miniwsa.repository.projection.TopPathProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    @Query(value = "SELECT se FROM SecurityEvent se WHERE se.clientIp = :clientIp " +
                   "AND se.timestamp >= :tenMinutesAgo ORDER BY se.timestamp DESC")
    List<SecurityEvent> findRecentEventsByClientIp(@Param("clientIp") String clientIp,
                                                   @Param("tenMinutesAgo") Long tenMinutesAgo);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.timestamp BETWEEN :from AND :to " +
           "AND (:configId IS NULL OR se.configId = :configId)")
    long countByTimeRangeAndConfig(@Param("from") long from,
                                   @Param("to") long to,
                                   @Param("configId") Long configId);

    @Query("SELECT r.category AS category, COUNT(se) AS count, AVG(se.threatScore) AS avgThreatScore " +
           "FROM SecurityEvent se JOIN se.rule r " +
           "WHERE se.timestamp BETWEEN :from AND :to " +
           "AND (:configId IS NULL OR se.configId = :configId) " +
           "GROUP BY r.category")
    List<CategoryStatsProjection> statsByCategory(@Param("from") long from,
                                                  @Param("to") long to,
                                                  @Param("configId") Long configId);

    @Query("SELECT se.action AS action, COUNT(se) AS count " +
           "FROM SecurityEvent se " +
           "WHERE se.timestamp BETWEEN :from AND :to " +
           "AND (:configId IS NULL OR se.configId = :configId) " +
           "GROUP BY se.action")
    List<ActionCountProjection> statsByAction(@Param("from") long from,
                                              @Param("to") long to,
                                              @Param("configId") Long configId);

    @Query("SELECT se.clientIp AS clientIp, COUNT(se) AS count, AVG(se.threatScore) AS avgThreatScore " +
           "FROM SecurityEvent se " +
           "WHERE se.timestamp BETWEEN :from AND :to " +
           "AND (:configId IS NULL OR se.configId = :configId) " +
           "GROUP BY se.clientIp ORDER BY COUNT(se) DESC")
    List<TopAttackerProjection> topAttackers(@Param("from") long from,
                                             @Param("to") long to,
                                             @Param("configId") Long configId,
                                             Pageable pageable);

    @Query("SELECT se.path AS path, COUNT(se) AS count " +
           "FROM SecurityEvent se " +
           "WHERE se.timestamp BETWEEN :from AND :to " +
           "AND (:configId IS NULL OR se.configId = :configId) " +
           "GROUP BY se.path ORDER BY COUNT(se) DESC")
    List<TopPathProjection> topPaths(@Param("from") long from,
                                     @Param("to") long to,
                                     @Param("configId") Long configId,
                                     Pageable pageable);

    @Query(value = "SELECT se FROM SecurityEvent se JOIN se.rule r WHERE " +
            "(:from IS NULL OR se.timestamp >= :from) AND (:to IS NULL OR se.timestamp <= :to) AND " +
            "(:configId IS NULL OR se.configId = :configId) AND (:action IS NULL OR se.action = :action) AND " +
            "(:category IS NULL OR r.category = :category)",
            countQuery = "SELECT COUNT(se) FROM SecurityEvent se JOIN se.rule r WHERE " +
                    "(:from IS NULL OR se.timestamp >= :from) AND (:to IS NULL OR se.timestamp <= :to) AND " +
                    "(:configId IS NULL OR se.configId = :configId) AND (:action IS NULL OR se.action = :action) AND " +
                    "(:category IS NULL OR r.category = :category)")
    Page<SecurityEvent> findSamples(@Param("configId") Long configId,
                                    @Param("from") Long from,
                                    @Param("to") Long to,
                                    @Param("category") RuleCategory category,
                                    @Param("action") Action action,
                                    Pageable pageable);
}

