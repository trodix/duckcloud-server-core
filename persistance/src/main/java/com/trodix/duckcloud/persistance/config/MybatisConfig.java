package com.trodix.duckcloud.persistance.config;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer config = new MapperScannerConfigurer();
        config.setBasePackage("com.trodix.duckcloud.persistance");
        return config;
    }

}
