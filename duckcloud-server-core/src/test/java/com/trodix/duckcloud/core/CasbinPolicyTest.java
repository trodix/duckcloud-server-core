package com.trodix.duckcloud.core;

import com.trodix.duckcloud.core.utils.PostgreSQLTestContainer;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CasbinPolicyTest extends PostgreSQLTestContainer {

    @Autowired
    private Enforcer casbinEnforcer;

    @Test
    public void checkRoleNodeAdminPermissions() {
        assertTrue(casbinEnforcer.enforce("role:node:admin", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("role:node:admin", "feature:node", "WRITE"));
        assertTrue(casbinEnforcer.enforce("role:node:admin", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodeEditorPermissions() {
        assertTrue(casbinEnforcer.enforce("role:node:editor", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("role:node:editor", "feature:node", "WRITE"));
        assertFalse(casbinEnforcer.enforce("role:node:editor", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodeViewerPermissions() {
        assertTrue(casbinEnforcer.enforce("role:node:viewer", "feature:node", "READ"));
        assertFalse(casbinEnforcer.enforce("role:node:viewer", "feature:node", "WRITE"));
        assertFalse(casbinEnforcer.enforce("role:node:viewer", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodePermissions() {
        assertTrue(casbinEnforcer.enforce("role:node:read", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("role:node:write", "feature:node", "WRITE"));
        assertTrue(casbinEnforcer.enforce("role:node:delete", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodePermissionsOnResource() {
        assertTrue(casbinEnforcer.enforce("role:node:read", "feature:node:27", "READ"));
        assertTrue(casbinEnforcer.enforce("role:node:write", "feature:node:27", "WRITE"));
        assertTrue(casbinEnforcer.enforce("role:node:delete", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkUser2HasPermissionAdmin() {
        assertTrue(casbinEnforcer.enforce("1adf1de8-4ffe-4290-9cc5-17698593b280", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("1adf1de8-4ffe-4290-9cc5-17698593b280", "feature:node", "WRITE"));
        assertTrue(casbinEnforcer.enforce("1adf1de8-4ffe-4290-9cc5-17698593b280", "feature:node", "DELETE"));
    }

    @Test
    public void checkPatrickHasPermissionEditor() {
        assertTrue(casbinEnforcer.enforce("patrick", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("patrick", "feature:node", "WRITE"));
        assertFalse(casbinEnforcer.enforce("patrick", "feature:node", "DELETE"));
    }

    @Test
    public void checkMichelHasPermissionViewer() {
        assertTrue(casbinEnforcer.enforce("michel", "feature:node", "READ"));
        assertFalse(casbinEnforcer.enforce("michel", "feature:node", "WRITE"));
        assertFalse(casbinEnforcer.enforce("michel", "feature:node", "DELETE"));
    }

    @Test
    public void checkMichelCanManageNode27() {
        assertTrue(casbinEnforcer.enforce("michel", "feature:node:27", "READ"));
        assertTrue(casbinEnforcer.enforce("michel", "feature:node:27", "WRITE"));
        assertTrue(casbinEnforcer.enforce("michel", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkJaneCanManageNode27() {
        assertTrue(casbinEnforcer.enforce("jane", "feature:node:27", "READ"));
        assertTrue(casbinEnforcer.enforce("jane", "feature:node:27", "WRITE"));
        assertTrue(casbinEnforcer.enforce("jane", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkJaneCanManageNodes() {
        assertFalse(casbinEnforcer.enforce("jane", "feature:node", "READ"));
        assertFalse(casbinEnforcer.enforce("jane", "feature:node", "WRITE"));
        assertFalse(casbinEnforcer.enforce("jane", "feature:node", "DELETE"));
    }

    @Test
    public void checkJohnCanManageNodes() {
        assertTrue(casbinEnforcer.enforce("john", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("john", "feature:node", "WRITE"));
        assertTrue(casbinEnforcer.enforce("john", "feature:node", "DELETE"));
    }

    @Test
    public void checkJohnCanManageNode27() {
        assertTrue(casbinEnforcer.enforce("john", "feature:node:27", "READ"));
        assertTrue(casbinEnforcer.enforce("john", "feature:node:27", "WRITE"));
        assertTrue(casbinEnforcer.enforce("john", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkTitouanIsEditorByInheritance() {
        assertTrue(casbinEnforcer.enforce("titouan", "feature:node", "READ"));
        assertTrue(casbinEnforcer.enforce("titouan", "feature:node", "WRITE"));
        assertFalse(casbinEnforcer.enforce("titouan", "feature:node", "DELETE"));
    }

}
