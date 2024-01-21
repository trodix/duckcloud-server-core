package com.trodix.duckcloud.domain.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

	@Value("${app.elasticsearch-url}")
	private String elasticsearchUrl;

	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()           
			.connectedTo(elasticsearchUrl)
			.build();
	}

	@Bean
	public ElasticsearchClient elasticsearchClient(RestClient restClient, JsonpMapper jsonpMapper) {
		RestClientTransport transport = new RestClientTransport(restClient, jsonpMapper);
		return new ElasticsearchClient(transport);
	}

	@Bean
	public JsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
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