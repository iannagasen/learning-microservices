package dev.agasen.microsrv.core.recommendation.services;

import java.util.logging.Level;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import dev.agasen.microsrv.core.recommendation.persistence.RecommendationEntity;
import dev.agasen.microsrv.core.recommendation.persistence.RecommendationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
@AllArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

  private final ServiceUtil serviceUtil;
  private final RecommendationMapper recommendationMapper;
  private final RecommendationRepository recommendationRepository;



  @Override
  public Flux<Recommendation> getRecommendations(int productId) {

    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    log.info("Will get recommendations for product with id={}", productId);

    return recommendationRepository
        .findByProductId(productId)
        .log(log.getName(), Level.FINE)
        .map(recommendationMapper::entityToApi)
        .map(this::setServiceAddress);
  }



  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {

    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

    RecommendationEntity entity = recommendationMapper.apiToEntity(body);
    return recommendationRepository
        .save(entity)
        .log(log.getName(), Level.FINE)
        .onErrorMap(DuplicateKeyException.class, (e) -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
        .map(recommendationMapper::entityToApi);
  }



  @Override
  public Mono<Void> deleteRecommendations(int productId) {

    log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
    return recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId));
  }



  private Recommendation setServiceAddress(Recommendation e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
  
}
