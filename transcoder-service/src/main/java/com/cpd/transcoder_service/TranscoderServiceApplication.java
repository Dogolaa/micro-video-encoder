package com.cpd.transcoder_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TranscoderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranscoderServiceApplication.class, args);
	}

}
