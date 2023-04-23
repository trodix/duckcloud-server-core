package com.trodix.duckcloud.domain.search.config;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchJacksonConfig {

    @Bean
    public JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
        ObjectMapper elasticSearchobjectMapper = makeElasticCompatible(objectMapper);

        // add java 8 date serialization / deserialization feature
        elasticSearchobjectMapper.registerModule(new JavaTimeModule());

        return new JacksonJsonpMapper(elasticSearchobjectMapper);
    }

    private ObjectMapper makeElasticCompatible(ObjectMapper objectMapper) {
        if (!objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            return objectMapper;
        }

        return objectMapper.copy().disable(SerializationFeature.INDENT_OUTPUT);
    }

}