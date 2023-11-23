package dev.agasen.microsrv.core.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import dev.agasen.microsrv.core.review.config.AppConfig;

@ComponentScan(basePackages = { "dev.agasen", "se.magnus" })
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class ReviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewServiceApplication.class, args);
	}

}
