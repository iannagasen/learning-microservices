package dev.agasen.microsrv.core.composite.product.services;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.agasen.microsrv.core.composite.product.config.AppPropertiesConfig;
import lombok.AllArgsConstructor;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;

@Component
@AllArgsConstructor
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
  
}
