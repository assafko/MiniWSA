package com.miniwsa.repository.projection;

public interface TopAttackerProjection {
    String getClientIp();
    long getCount();
    Double getAvgThreatScore();
}
