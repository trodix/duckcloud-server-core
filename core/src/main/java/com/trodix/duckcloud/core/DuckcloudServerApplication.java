package com.trodix.duckcloud.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com.trodix.duckcloud")
@EnableScheduling
public class DuckcloudServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuckcloudServerApplication.class, args);
	}

}
