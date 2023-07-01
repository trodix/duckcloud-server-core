package com.trodix.duckcloud.security.persistance.dao.mappers;

import com.trodix.duckcloud.security.persistance.entities.OwnerScopeQuery;
import com.trodix.duckcloud.security.persistance.entities.Permission;
import com.trodix.duckcloud.security.persistance.entities.ResourceScopeQuery;
import com.trodix.duckcloud.security.persistance.entities.ScopeQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<Permission> findPermissionsForResource(ResourceScopeQuery resourceScope);

    List<Permission> findPermissionForOwnerAndResource(ScopeQuery scope);

    List<Permission> findPermissionForOwnerAndResourceType(ScopeQuery scope);

    void insertPermission(Permission permission);

    void updatePermission(Permission permission);

    void deletePermission(ScopeQuery scope);

}
