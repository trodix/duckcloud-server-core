package com.trodix.duckcloud.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.trodix.duckcloud")
public class DuckcloudServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuckcloudServerApplication.class, args);
	}

}
