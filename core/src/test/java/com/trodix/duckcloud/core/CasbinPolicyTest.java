package com.trodix.duckcloud.core;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.duckcloud.core.utils.PostgreSQLTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CasbinPolicyTest extends PostgreSQLTestContainer {

    @Autowired
    private EnforcerApi enforcer;

    @Test
    public void checkRoleNodeAdminPermissions() {
        assertTrue(enforcer.enforce("role:node:admin", "feature:node", "READ"));
        assertTrue(enforcer.enforce("role:node:admin", "feature:node", "WRITE"));
        assertTrue(enforcer.enforce("role:node:admin", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodeEditorPermissions() {
        assertTrue(enforcer.enforce("role:node:editor", "feature:node", "READ"));
        assertTrue(enforcer.enforce("role:node:editor", "feature:node", "WRITE"));
        assertFalse(enforcer.enforce("role:node:editor", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodeViewerPermissions() {
        assertTrue(enforcer.enforce("role:node:viewer", "feature:node", "READ"));
        assertFalse(enforcer.enforce("role:node:viewer", "feature:node", "WRITE"));
        assertFalse(enforcer.enforce("role:node:viewer", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodePermissions() {
        assertTrue(enforcer.enforce("role:node:read", "feature:node", "READ"));
        assertTrue(enforcer.enforce("role:node:write", "feature:node", "WRITE"));
        assertTrue(enforcer.enforce("role:node:delete", "feature:node", "DELETE"));
    }

    @Test
    public void checkRoleNodePermissionsOnResource() {
        assertTrue(enforcer.enforce("role:node:read", "feature:node:27", "READ"));
        assertTrue(enforcer.enforce("role:node:write", "feature:node:27", "WRITE"));
        assertTrue(enforcer.enforce("role:node:delete", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkUser2HasPermissionAdmin() {
        assertTrue(enforcer.enforce("1adf1de8-4ffe-4290-9cc5-17698593b280", "feature:node", "READ"));
        assertTrue(enforcer.enforce("1adf1de8-4ffe-4290-9cc5-17698593b280", "feature:node", "WRITE"));
        assertTrue(enforcer.enforce("1adf1de8-4ffe-4290-9cc5-17698593b280", "feature:node", "DELETE"));
    }

    @Test
    public void checkPatrickHasPermissionEditor() {
        assertTrue(enforcer.enforce("patrick", "feature:node", "READ"));
        assertTrue(enforcer.enforce("patrick", "feature:node", "WRITE"));
        assertFalse(enforcer.enforce("patrick", "feature:node", "DELETE"));
    }

    @Test
    public void checkMichelHasPermissionViewer() {
        assertTrue(enforcer.enforce("michel", "feature:node", "READ"));
        assertFalse(enforcer.enforce("michel", "feature:node", "WRITE"));
        assertFalse(enforcer.enforce("michel", "feature:node", "DELETE"));
    }

    @Test
    public void checkMichelCanManageNode27() {
        assertTrue(enforcer.enforce("michel", "feature:node:27", "READ"));
        assertTrue(enforcer.enforce("michel", "feature:node:27", "WRITE"));
        assertTrue(enforcer.enforce("michel", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkJaneCanManageNode27() {
        assertTrue(enforcer.enforce("jane", "feature:node:27", "READ"));
        assertTrue(enforcer.enforce("jane", "feature:node:27", "WRITE"));
        assertTrue(enforcer.enforce("jane", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkJaneCanManageNodes() {
        assertFalse(enforcer.enforce("jane", "feature:node", "READ"));
        assertFalse(enforcer.enforce("jane", "feature:node", "WRITE"));
        assertFalse(enforcer.enforce("jane", "feature:node", "DELETE"));
    }

    @Test
    public void checkJohnCanManageNodes() {
        assertTrue(enforcer.enforce("john", "feature:node", "READ"));
        assertTrue(enforcer.enforce("john", "feature:node", "WRITE"));
        assertTrue(enforcer.enforce("john", "feature:node", "DELETE"));
    }

    @Test
    public void checkJohnCanManageNode27() {
        assertTrue(enforcer.enforce("john", "feature:node:27", "READ"));
        assertTrue(enforcer.enforce("john", "feature:node:27", "WRITE"));
        assertTrue(enforcer.enforce("john", "feature:node:27", "DELETE"));
    }

    @Test
    public void checkTitouanIsEditorByInheritance() {
        assertTrue(enforcer.enforce("titouan", "feature:node", "READ"));
        assertTrue(enforcer.enforce("titouan", "feature:node", "WRITE"));
        assertFalse(enforcer.enforce("titouan", "feature:node", "DELETE"));
    }

}
