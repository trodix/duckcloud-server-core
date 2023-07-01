package com.trodix.duckcloud.security.persistance.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OwnerScopeQuery {
    private OwnerUser ownerUser;

    private OwnerAuthorities ownerAuthorities;


    @Data
    @AllArgsConstructor
    public static class OwnerUser {
        public static final OwnerType ownerType = OwnerType.USER;
        private String userId;
    }

    @Data
    @AllArgsConstructor
    public static class OwnerAuthorities {
        public static final OwnerType ownerType = OwnerType.ROLE;
        private List<String> grantedAuthorityList;
    }
}
