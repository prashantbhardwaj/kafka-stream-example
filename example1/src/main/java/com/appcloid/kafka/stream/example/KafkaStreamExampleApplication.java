package com.appcloid.kafka.stream.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaStreamExampleApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamExampleApplication.class);

	public static void main(String[] args) {
		LOGGER.info("Starting the application");
		SpringApplication.run(KafkaStreamExampleApplication.class, args);
	}
}