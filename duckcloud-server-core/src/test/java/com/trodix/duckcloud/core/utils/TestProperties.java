package com.trodix.duckcloud.core.utils;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class TestProperties {

    @DynamicPropertySource
    private static void setupProperties(DynamicPropertyRegistry registry) {
        registry.add("casbin.policy", () -> "classpath:casbin/policy-test.csv");
    }

}
