package dev.agasen.microsrv.core.composite.product.services;

import java.io.IOException;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.agasen.microsrv.core.composite.product.config.AppPropertiesConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Component
@AllArgsConstructor
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;
  private final AppPropertiesConfig appConfig;

  @Override
  public List<Review> getReviews(int productId) {
    return restTemplate.exchange(
      "%s?productId=%d".formatted(appConfig.getReviewServiceUrl(), productId), 
      HttpMethod.GET, 
      null, 
      new ParameterizedTypeReference<List<Review>>() {}
    ).getBody();
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    return restTemplate.exchange(
      "%s?productId=%d".formatted(appConfig.getRecommendationServiceUrl(), productId), 
      HttpMethod.GET, 
      null, 
      new ParameterizedTypeReference<List<Recommendation>>() {}
    ).getBody();
  }

  @Override
  public Product getProduct(int productId) {
    return restTemplate.getForObject(
      appConfig.getProductServiceUrl() + "/" + productId, 
      Product.class
    );
  }

  @Override
  public Product createProduct(Product body) {
    try {
      Product product =  restTemplate.postForObject(appConfig.getProductServiceUrl(), body, Product.class);
      return product;
    } catch (HttpClientErrorException e) {
      throw handleHttpClientException(e);
    }
  }

  @Override
  public void deleteProduct(int productId) {
    try {
      restTemplate.delete(appConfig.getProductServiceUrl() + "/" + productId);
    } catch (HttpClientErrorException e) {
      throw handleHttpClientException(e);
    }
  }
  

  private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
    switch (HttpStatus.resolve(ex.getStatusCode().value())) {

      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(ex));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(ex));

      default:
        log.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        log.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }
  }

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }

  @Override
  public Review createReview(Review body) {
    try {
      String url = appConfig.getReviewServiceUrl();
      log.debug("Will post a new review to URL: {}", url);

      Review review = restTemplate.postForObject(url, body, Review.class);
      log.debug("Created a review with id: {}", review.getProductId());

      return review;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteReviews(int productId) {
    try {
      String url = appConfig.getProductServiceUrl() + "?productId=" + productId;
      log.debug("Will call the deleteReviews API on URL: {}", url);

      restTemplate.delete(url);

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      String url = appConfig.getRecommendationServiceUrl();
      log.debug("Will post a new recommendation to URL: {}", url);

      Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
      log.debug("Created a recommendation with id: {}", recommendation.getProductId());

      return recommendation;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteRecommendations(int productId) {
    try {
      String url = appConfig.getRecommendationServiceUrl() + "?productId=" + productId;
      log.debug("Will call the deleteRecommendations API on URL: {}", url);
      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }
}
