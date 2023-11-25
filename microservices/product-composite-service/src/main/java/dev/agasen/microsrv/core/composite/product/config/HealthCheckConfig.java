package dev.agasen.microsrv.core.composite.product.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.agasen.microsrv.core.composite.product.services.ProductCompositeIntegration;

@Configuration
public class HealthCheckConfig {

  @Autowired
  ProductCompositeIntegration integration;

  @Bean
  ReactiveHealthContributor coreServices() {
    /**
     * This is useful to check all the microservices are healthy before 
     * /actuator/health returns a value
     * 
     * It is useful when testing, to make sure that all the microservices are running
     * Ex: 
     *  > waitForService curl http://$HOST:$PORT/actuator/health
     *  - where waitForService is a function that wait for the service to return a value
     */
    final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

    registry.put("product", () -> integration.getProductHealth());
    registry.put("recommendation", () -> integration.getRecommendationHealth());
    registry.put("review", () -> integration.getReviewHealth());

    return CompositeReactiveHealthContributor.fromMap(registry);
  }
}
