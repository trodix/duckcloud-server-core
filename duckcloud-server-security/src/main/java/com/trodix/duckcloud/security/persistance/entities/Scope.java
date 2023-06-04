package com.trodix.duckcloud.security.persistance.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Scope {

    private OwnerScope ownerScope;
    private ResourceScope resourceScope;

}
