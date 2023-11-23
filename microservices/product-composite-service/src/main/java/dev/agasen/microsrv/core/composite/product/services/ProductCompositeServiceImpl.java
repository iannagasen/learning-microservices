package dev.agasen.microsrv.core.composite.product.services;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.ProductCompositeService;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.composite.product.ServiceAddresses;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.http.ServiceUtil;

@RestController
@AllArgsConstructor
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {
  
  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;



  @Override
  public Mono<ProductAggregate> getProduct(int productId) {

    log.info("Will get composite product info for product.id={}", productId);

    return Mono
        .zip(
            integration.getProduct(productId),
            Mono.from(integration.getRecommendations(productId).collectList()), // collect flux at once
            Mono.from(integration.getReviews(productId).collectList()) // collect flux at once
        )
        .flatMap(tuple -> {
            Product product = tuple.getT1();
            List<Recommendation> recommendations = tuple.getT2();
            List<Review> reviews = tuple.getT3();
            return Mono.just(createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress()));
        })
        .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
        .log(log.getName(), Level.FINE);
  }
  


  @Override
  public Mono<Void> createProduct(ProductAggregate body) {

    log.info("Will create a new composite entity for product.id: {}", body.getProductId());
    
    return Mono.just(body)
        .flatMap(this::createProductMapper)
        .onErrorMap(RuntimeException.class, re -> {
          log.warn("createCompositeProduct failed: {}", re.toString());
          return re;
        });
  }



  @Override
  public Mono<Void> deleteProduct(int productId) {
    
    log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

    return Mono.just(productId)
        .flatMap(id -> Mono
            .zip(
                integration.deleteProduct(id),
                integration.deleteRecommendations(id),
                integration.deleteReviews(id))
            .doOnError(ex -> log.warn("deleteProduct failed: {}", ex.toString()))
            .log(log.getName(), Level.FINE)
        )
        .onErrorMap(RuntimeException.class, re -> { // this is similar to ReThrowing
          log.warn("deleteCompositeProduct failed: {}", re.toString());
          return re;
        })
        .then();
  }



  /**
   * use in mapping Create Product Aggregates to Mono<Void>
   * used in this.createProduct()
   */
  public Mono<Void> createProductMapper(ProductAggregate aggregate) {

      Product product = new Product(aggregate.getProductId(), aggregate.getName(), aggregate.getWeight(), null);
      Mono<Product> productMono = integration.createProduct(product);

      Flux<Mono<Void>> recommendationMonos = aggregate.getRecommendations() != null
          ? Flux.fromIterable(aggregate.getRecommendations())
              .map(r -> integration.createRecommendation(new Recommendation(aggregate.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null)))
              .map(Mono::then)
          : Flux.empty();

      Flux<Mono<Void>> reviewMonos = aggregate.getReviews() != null
          ? Flux.fromIterable(aggregate.getReviews())
              .map(r -> integration.createReview(new Review(aggregate.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null)))
              .map(Mono::then)
          : Flux.empty();

      return Mono.when(productMono)
          .thenMany(Flux.concat(reviewMonos, recommendationMonos))
          .doOnError(ex -> log.warn("createProduct failed"))
          .log(log.getName(), Level.FINE)
          .then();
  }



  private ProductAggregate createProductAggregate(
    Product product,
    List<Recommendation> recommendations,
    List<Review> reviews,
    String serviceAddress) {

    // 1. Setup product info
    int productId = product.getProductId();
    String name = product.getName();
    int weight = product.getWeight();

    // 2. Copy summary recommendation info, if available
    List<RecommendationSummary> recommendationSummaries =
      (recommendations == null) ? null : recommendations.stream()
        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
        .collect(Collectors.toList());

    // 3. Copy summary review info, if available
    List<ReviewSummary> reviewSummaries = 
      (reviews == null) ? null : reviews.stream()
        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
        .collect(Collectors.toList());

    // 4. Create info regarding the involved microservices addresses
    String productAddress = product.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }

}
