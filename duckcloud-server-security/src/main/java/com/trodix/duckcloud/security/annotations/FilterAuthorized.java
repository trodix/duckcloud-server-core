package com.trodix.duckcloud.security.annotations;

import com.trodix.duckcloud.security.models.PermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterAuthorized {

    String resourceType();

    PermissionType permissionType();

}
