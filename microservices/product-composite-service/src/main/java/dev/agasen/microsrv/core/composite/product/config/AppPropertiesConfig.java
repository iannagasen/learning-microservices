package dev.agasen.microsrv.core.composite.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "app")
@Getter
public class AppPropertiesConfig {
  
  private ServiceProp productService;
  private ServiceProp recommendationService;
  private ServiceProp reviewService;

  @ConstructorBinding
  public AppPropertiesConfig(ServiceProp productService, ServiceProp recommendationService, ServiceProp reviewService) {
    this.productService = productService;
    this.recommendationService = recommendationService;
    this.reviewService = reviewService;
  }

  public String getProductServiceUrl() {
    return productService.getUrl();
  }

  public String getRecommendationServiceUrl() {
    return recommendationService.getUrl();
  }

  public String getReviewServiceUrl() {
    return reviewService.getUrl();
  }

  @Getter
  public static class ServiceProp {
    // private String host;
    // private int port;
    private String url;
    
    @ConstructorBinding
    public ServiceProp(String url) {
      this.url = url;
    }
  }
}
