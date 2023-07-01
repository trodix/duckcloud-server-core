package com.trodix.duckcloud.security.annotations;

import com.trodix.duckcloud.security.persistance.entities.*;
import com.trodix.duckcloud.security.services.AuthenticationService;
import com.trodix.duckcloud.security.services.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
@RequiredArgsConstructor
@Aspect
@Slf4j
public class FilterAuthorizedAspect {

    private final AuthenticationService authenticationService;

    private final PermissionService permissionService;

    @Pointcut("@annotation(FilterAuthorized)")
    public void filterAuthorizedPointcut() {

    }

    @Around("filterAuthorizedPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object object = joinPoint.proceed();

        if (object instanceof List) {
            return filterList(joinPoint, object);
        }

        // TODO handle single object
        throw new IllegalArgumentException("Type " + object.getClass() + " is not supported for annotation " + FilterAuthorized.class);
    }

    /**
     * Handle return type List
     * @param joinPoint
     * @return The filtered list
     * @throws Throwable
     */
    public Object filterList(ProceedingJoinPoint joinPoint, Object object) throws Throwable {
        List<Object> returnObjects = (List<Object>) object;

        if (returnObjects.isEmpty()) {
            return returnObjects;
        }

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());

        Class resourceType = returnObjects.get(0).getClass();
        Class returnType = methodSignature.getReturnType();
        Class supported = List.class;
        if (!returnType.equals(supported)) {
            throw new IllegalAccessException("Return type must be of type " + List.class.getName() + ", found " + returnType.getName());
        }

        ScopeQuery scope = ScopeQuery.builder()
                .ownerScope(
                        OwnerScopeQuery.builder()
                                .ownerUser(
                                        new OwnerScopeQuery.OwnerUser(authenticationService.getUserId())
                                )
                                .ownerAuthorities(
                                        new OwnerScopeQuery.OwnerAuthorities(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map(a -> a.getAuthority()).toList())
                                )
                                .build()
                )
                .resourceScope(
                        ResourceScopeQuery.builder()
                                .resourceType(resourceType.getName())
                                .build()
                )
                .build();

        List<Permission> permissions = permissionService.getPermissionForOwnerAndResourceType(scope);

        Set<Object> filtered = new HashSet<>();

        for (Permission p : permissions) {
            for (Object o : returnObjects) {
                String resourceId = findResourceIdFromAnnotatedField(o)
                        .orElseThrow(() -> new IllegalArgumentException("No field annotated with " + FilterResourceId.class + " found for class " + o.getClass()));
                if (p.getResourceType().equals(o.getClass().getName()) && (p.getResourceId() == null || p.getResourceId().equals(resourceId))) {
                    log.debug("Authorization granted for permission {} on resource {}", p, resourceId);
                    filtered.add(o);
                } else {
                    log.debug("Authorization denied for permission {} on resource {}", p, resourceId);
                }
            }
        }

        return filtered.stream().toList();
    }

    private Optional<String> findResourceIdFromAnnotatedField(Object object) {
        try {
            Field field = Arrays.stream(object.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(FilterResourceId.class))
                    .findFirst()
                    .orElseThrow();

            field.setAccessible(true);
            String value = String.valueOf(field.get(object));
            field.setAccessible(false);

            return Optional.of(value);
        } catch (Exception e) {
            log.error("An error occurred while trying to get resource id field on object " + object.getClass(), e);
        }
        return Optional.empty();
    }

}
