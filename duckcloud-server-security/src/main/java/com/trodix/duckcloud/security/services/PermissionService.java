package com.trodix.duckcloud.security.services;

import com.trodix.duckcloud.security.persistance.dao.mappers.PermissionMapper;
import com.trodix.duckcloud.security.persistance.entities.OwnerScopeQuery;
import com.trodix.duckcloud.security.persistance.entities.Permission;
import com.trodix.duckcloud.security.persistance.entities.ResourceScopeQuery;
import com.trodix.duckcloud.security.persistance.entities.ScopeQuery;
import com.trodix.duckcloud.security.models.PermissionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionMapper permissionMapper;

    public List<Permission> getPermissionsForResource(ResourceScopeQuery resourceScope) {
        List<Permission> result = permissionMapper.findPermissionsForResource(resourceScope);
        log.debug("{} permission(s) found for {}", result.size(), resourceScope);
        return result;
    }

    public List<Permission> getPermissionForOwnerAndResource(ScopeQuery scope) {
        List<Permission> result = permissionMapper.findPermissionForOwnerAndResource(scope);
        log.debug("Permission {} for {}", result.isEmpty() ? "not found" : "found", scope);
        return result;
    }

    public List<Permission> getPermissionForOwnerAndResourceType(ScopeQuery scope) {
        List<Permission> result = permissionMapper.findPermissionForOwnerAndResourceType(scope);
        log.debug("Permission {} for {}", result.isEmpty() ? "not found" : "found", scope);
        return result;
    }

    public Permission createPermission(Permission permission) {
        log.debug("Creating new permission ({})", permission);

        permissionMapper.insertPermission(permission);

        return permission;
    }

    public void updatePermission(Permission permission) {
        log.debug("Updating permission {}", permission);
        permissionMapper.updatePermission(permission);
    }

    public void deletePermission(ScopeQuery scope) {
        log.debug("Deleting permission {}", scope);
        permissionMapper.deletePermission(scope);
    }

    public boolean hasPermission(ScopeQuery scope, PermissionType permissionType) {

        List<Permission> matchingPermissions = getPermissionForOwnerAndResource(scope);

        if (matchingPermissions.isEmpty()) {
            log.debug("No permissions found found for {}", scope);
            return false;
        }

        for (Permission permission : matchingPermissions) {

            if (PermissionType.READ == permissionType) {
                return permission.isRead();
            } else if (PermissionType.CREATE == permissionType) {
                return permission.isCreate();
            } else if (PermissionType.UPDATE == permissionType) {
                return permission.isUpdate();
            } else if (PermissionType.DELETE == permissionType) {
                return permission.isDelete();
            } else {
                throw new IllegalArgumentException("Permission " + permissionType + " not supported");
            }

        }

        return false;
    }

}
