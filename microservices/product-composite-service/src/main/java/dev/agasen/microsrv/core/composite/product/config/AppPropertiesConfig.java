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
    return productService.getUrl() + "/product";
  }

  public String getRecommendationServiceUrl() {
    return recommendationService.getUrl() + "/recommendation";
  }

  public String getReviewServiceUrl() {
    return reviewService.getUrl() + "/review";
  }

  @Getter
  public static class ServiceProp {
    private String host;
    private int port;
    
    @ConstructorBinding
    public ServiceProp(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public String getUrl() {
      return "http://%s:%d".formatted(host, port);
    }
  }
}
