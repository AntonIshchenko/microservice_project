package com.example.storageservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class StorageServiceApplication {

	public static Logger logger = LoggerFactory.getLogger(StorageServiceApplication.class);

	public static void main(String[] args) {
		logger.info("application starting");
		logger.error("application starting");
		logger.debug("application starting");
		SpringApplication.run(StorageServiceApplication.class, args);
	}

}
