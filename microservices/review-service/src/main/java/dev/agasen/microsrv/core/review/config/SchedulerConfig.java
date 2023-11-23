package dev.agasen.microsrv.core.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
  
  private final AppConfig appConfig;

  @Bean
  public Scheduler jdbcScheduler() {
    log.info("Creates a jdbcScheduler with thread pool size = {}", appConfig.getThreadPoolSize());
    return Schedulers.newBoundedElastic(appConfig.getThreadPoolSize(), appConfig.getTaskQueueSize(), "jdbc-pool");
  }
  
}
