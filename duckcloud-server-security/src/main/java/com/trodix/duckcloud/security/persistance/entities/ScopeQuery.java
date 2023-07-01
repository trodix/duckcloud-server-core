package com.trodix.duckcloud.security.persistance.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScopeQuery {

    private OwnerScopeQuery ownerScope;
    private ResourceScopeQuery resourceScope;

}