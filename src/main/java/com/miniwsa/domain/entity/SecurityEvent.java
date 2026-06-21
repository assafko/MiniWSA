package com.miniwsa.domain.entity;

import com.miniwsa.domain.enums.Action;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_client_ip", columnList = "client_ip"),
        @Index(name = "idx_received_at", columnList = "received_at"),
        @Index(name = "idx_client_ip_received_at", columnList = "client_ip,received_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientIp;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String httpMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private Long receivedAt;

    @Column(nullable = false)
    private String attackType;

    @Column(nullable = false)
    private Integer threatScore;

    // Additional fields aligned with incoming request
    @Column
    private String eventId;

    @Column
    private Long configId;

    @Column
    private String policyId;

    @Column
    private String hostname;

    @Column
    private Integer statusCode;

    @Column
    private String userAgent;

    @Column(name = "geo_country")
    private String geoCountry;

    @Column(name = "geo_city")
    private String geoCity;

    @Column
    private Integer requestSize;

    @Column
    private Integer responseSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        if (receivedAt == null) {
            receivedAt = System.currentTimeMillis();
        }
    }
}

