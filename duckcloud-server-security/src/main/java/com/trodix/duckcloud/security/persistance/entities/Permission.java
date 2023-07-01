package com.trodix.duckcloud.security.persistance.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Permission {

    private long id;
    private String ownerType;
    private String ownerId;
    private String resourceType;
    private String resourceId;
    private boolean read;
    private boolean create;
    private boolean update;
    private boolean delete;

}
