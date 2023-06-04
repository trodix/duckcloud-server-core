package com.trodix.duckcloud.security.annotations;

import com.trodix.duckcloud.security.models.PermissionType;
import com.trodix.duckcloud.security.persistance.entities.OwnerScopeQuery;
import com.trodix.duckcloud.security.persistance.entities.ResourceScopeQuery;
import com.trodix.duckcloud.security.persistance.entities.ScopeQuery;
import com.trodix.duckcloud.security.services.AuthenticationService;
import com.trodix.duckcloud.security.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Aspect
public class AuthorizationAspect {

    private final AuthenticationService authenticationService;

    private final PermissionService permissionService;

    @Pointcut("@annotation(Authorization)")
    public void authorizationPointcut() {

    }

    @Around("authorizationPointcut()")
    public void aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        Authorization authz = methodSignature.getMethod().getAnnotation(Authorization.class);

        Map<String, Object> annotatedParameterValue = getAuthResourceIdAnnotatedParameterValue(methodSignature.getMethod(), joinPoint.getArgs());

        boolean granted;

        if (annotatedParameterValue.isEmpty()) {
            // case: create, we don't need permission on a resourceId
            if (!authz.permissionType().equals(PermissionType.CREATE)) {
                throw new IllegalArgumentException("You need to annotate a method parameter with @ResourceId");
            }
            granted = handleAuthorizationForUser(authz);
        } else {
            String resourceId = annotatedParameterValue.values().stream().toList().get(0).toString();
            granted = handleAuthorizationForResource(authz, resourceId);
        }

        if (granted) {
            joinPoint.proceed();
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this resource.");
    }

    private boolean handleAuthorizationForUser(Authorization authz) {
        String userId = authenticationService.getUserId();

        List<GrantedAuthority> grantedAuthorityList = (List<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        List<String> roles = grantedAuthorityList.stream().map(p -> p.getAuthority()).toList();

        ScopeQuery scope = ScopeQuery.builder()
                .ownerScope(
                        OwnerScopeQuery.builder()
                                .ownerUser(new OwnerScopeQuery.OwnerUser(userId))
                                .ownerAuthorities(new OwnerScopeQuery.OwnerAuthorities(roles))
                                .build()
                )
                .resourceScope(
                        ResourceScopeQuery.builder()
                                .resourceType(authz.resourceType().getName())
                                .build()
                )
                .build();

        PermissionType permissionType = authz.permissionType();

        return permissionService.hasPermission(scope, permissionType);
    }

    private boolean handleAuthorizationForResource(Authorization authz, String resourceId) {
        String userId = authenticationService.getUserId();

        List<GrantedAuthority> grantedAuthorityList = (List<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        List<String> roles = grantedAuthorityList.stream().map(p -> p.getAuthority()).toList();

        ScopeQuery scope = ScopeQuery.builder()
                .ownerScope(
                        OwnerScopeQuery.builder()
                                .ownerUser(new OwnerScopeQuery.OwnerUser(userId))
                                .ownerAuthorities(new OwnerScopeQuery.OwnerAuthorities(roles))
                                .build()
                )
                .resourceScope(
                        ResourceScopeQuery.builder()
                                .resourceType(authz.resourceType().getName())
                                .resourceId(resourceId)
                                .build()
                )
                .build();

        PermissionType permissionType = authz.permissionType();

        return permissionService.hasPermission(scope, permissionType);
    }

    private Map<String, Object> getAuthResourceIdAnnotatedParameterValue(Method method, Object[] args) {
        Map<String, Object> annotatedParameters = new HashMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Parameter[] parameters = method.getParameters();

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            Object arg = args[i];
            String name = parameters[i++].getDeclaringExecutable().getName();
            for (Annotation annotation : annotations) {
                if (annotation instanceof AuthResourceId) {
                    annotatedParameters.put(name, arg);
                }
            }
        }
        return annotatedParameters;
    }



}
