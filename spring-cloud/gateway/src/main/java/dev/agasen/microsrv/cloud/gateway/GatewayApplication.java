package dev.agasen.microsrv.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}


	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder() { 
		/**
		 * This is load balanced which makes it aware of microservice instances registered in the discovery server
		 */
		return WebClient.builder();
	}
}
