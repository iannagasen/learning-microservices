package dev.agasen.microsrv.core.composite.product.services;

import java.io.IOException;
import java.util.logging.Level;

import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.agasen.microsrv.core.composite.product.config.AppPropertiesConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.api.event.Event.Type;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Component
@AllArgsConstructor
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

  private final WebClient webClient;
  private final ObjectMapper mapper;
  private final AppPropertiesConfig appConfig;
  private final StreamBridge streamBridge; // this sends a message to output channels at runtime
  private final Scheduler publishEventScheduler;



  @Override
  public Flux<Review> getReviews(int productId) {
    
    String url = "%s?productId=%d".formatted(appConfig.getReviewServiceUrl(), productId);

    log.debug("Will call the getReviews API on URL: {}", url);

    return webClient.get().uri(url).retrieve()
        .bodyToFlux(Review.class)
        .log(log.getName(), Level.FINE)
        .onErrorResume(error -> Flux.empty()); // return empty Flux if have errors
  }



  @Override
  public Flux<Recommendation> getRecommendations(int productId) {

    String url = "%s?productId=%d".formatted(appConfig.getRecommendationServiceUrl(), productId);

    log.debug("Will call the getRecommendations API on URL: {}", url);

    return webClient.get().uri(url).retrieve()
      .bodyToFlux(Recommendation.class)
      .log(log.getName(), Level.FINE)
      .onErrorResume(error -> Flux.empty()); // return empty Flux if have errors
  }



  @Override
  public Mono<Product> getProduct(int productId) {

    String url = appConfig.getProductServiceUrl() + "/" + productId;

    log.debug("Will call the getProduct API on URL: {}", url);

    return webClient.get().uri(url).retrieve()
        .bodyToMono(Product.class)
        .log(log.getName(), Level.FINE)
        .onErrorMap(WebClientResponseException.class, this::handleException);
  }



  @Override
  public Mono<Product> createProduct(Product body) {
  
    log.info("ProductCompositeIntegration::createProduct");
    
    return Mono
      .fromCallable(() -> {
        sendMesage("products-out-0", new Event<>(Type.CREATE, body.getProductId(), body));
        return body;
      })
      .subscribeOn(publishEventScheduler);
  }



  @Override
  public Mono<Void> deleteProduct(int productId) {
    
    return Mono
        .fromRunnable(() -> sendMesage("products-out-0", new Event<>(Type.DELETE, productId, null)))
        .subscribeOn(publishEventScheduler)
        .then(); // convert from Mono<Object> to Mono<Void>
  }



  @Override
  public Mono<Review> createReview(Review body) {

    return Mono
        .fromCallable(() -> {
          sendMesage("reviews-out-0", new Event<>(Type.CREATE, body.getProductId(), body));
          return body;
        })
        .subscribeOn(publishEventScheduler);
  }



  @Override
  public Mono<Void> deleteReviews(int productId) {

    return Mono
        .fromRunnable(() -> sendMesage("reviews-out-0", new Event<>(Type.DELETE, productId, null)))
        .subscribeOn(publishEventScheduler)
        .then();
  }



  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {

    return Mono
        .fromCallable(() -> {
          sendMesage("recommendations-out-0", new Event<>(Type.CREATE, body.getProductId(), body));
          return body;
        })
        .subscribeOn(publishEventScheduler);
  }



  @Override
  public Mono<Void> deleteRecommendations(int productId) {

    return Mono
        .fromRunnable(() -> sendMesage("recommendations-out-0", new Event<>(Type.DELETE, productId, null)))
        .subscribeOn(publishEventScheduler)
        .then();
  }



  public Mono<Health> getProductHealth() {
    return getHealth(appConfig.getProductServiceUrl());
  }



  public Mono<Health> getRecommendationHealth() {
    return getHealth(appConfig.getRecommendationServiceUrl());
  }



  public Mono<Health> getReviewHealth() {
    return getHealth(appConfig.getRecommendationServiceUrl());
  }


  
  private Mono<Health> getHealth(String url) {
    url += "/actuator/health";
    log.debug("Will call the Health API on URL: {}", url);
    return webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .map(s -> new Health.Builder().up().build())
        .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
        .log(log.getName());
  }


  
  private <K, T> void sendMesage(String bindingName, Event<K, T> event) {

    log.debug("Sending a {} message to {}", event.getEventType(), bindingName);

    Message<Event<K, T>> message = MessageBuilder.withPayload(event)
        .setHeader("partitionKey", event.getKey())
        .build();

    streamBridge.send(bindingName, message);
  }



  private Throwable handleException(Throwable ex) {
    if (!(ex instanceof WebClientResponseException)) {
      log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
      return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException) ex;
    switch (HttpStatus.resolve(wcre.getStatusCode().value())) {

      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(wcre));

      default:
        log.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        log.warn("Error body: {}", wcre.getResponseBodyAsString());
        return wcre;
    }
  }


  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }


}
