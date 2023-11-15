package dev.agasen.microsrv.core.composite.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import dev.agasen.microsrv.core.composite.product.config.AppPropertiesConfig;

@ComponentScan(basePackages = { "dev.agasen", "se.magnus" })
@SpringBootApplication
@EnableConfigurationProperties(AppPropertiesConfig.class)
public class ProductCompositeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

}
