package dev.agasen.microsrv.core.composite.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import dev.agasen.microsrv.core.composite.product.config.AppPropertiesConfig;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@ComponentScan(basePackages = { "dev.agasen", "se.magnus" })
@SpringBootApplication
@EnableConfigurationProperties(AppPropertiesConfig.class)
public class ProductCompositeServiceApplication {

	@Value("${app.threadPoolSize:10}") 
	public Integer threadPoolSize;

	@Value("${app.taskQueueSize:100}") 
	public Integer taskQueueSize;

	@Bean
	public Scheduler publishEventScheduler() {
    log.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

}
