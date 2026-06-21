package com.miniwsa.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Simplified GeoLocation entity containing only `country` and `city` attributes.
 */
@Entity
@Table(name = "geo_locations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeoLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;
}

