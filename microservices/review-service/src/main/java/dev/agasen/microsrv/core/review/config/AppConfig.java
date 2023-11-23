package dev.agasen.microsrv.core.review.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app")
@Getter @Setter
public class AppConfig {
  
  private Integer threadPoolSize;
  private Integer taskQueueSize;
  
}
