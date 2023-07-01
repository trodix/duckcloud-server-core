package com.trodix.duckcloud.security.persistance.config;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisSecurityConfig {

    @Bean
    public MapperScannerConfigurer mapperSecurityScannerConfigurer() {
        MapperScannerConfigurer config = new MapperScannerConfigurer();
        config.setBasePackage("com.trodix.duckcloud.security.persistance");
        return config;
    }

}
