package com.miniwsa.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeoLocationDTO {
    private String country;
    private String city;
}

