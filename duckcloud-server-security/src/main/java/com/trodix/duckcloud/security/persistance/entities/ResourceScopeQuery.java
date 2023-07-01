package com.trodix.duckcloud.security.persistance.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceScopeQuery {
    private String resourceType;
    private String resourceId;
}
