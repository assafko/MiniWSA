package com.miniwsa.repository.projection;

import com.miniwsa.domain.enums.Action;

public interface ActionCountProjection {
    Action getAction();
    long getCount();
}
