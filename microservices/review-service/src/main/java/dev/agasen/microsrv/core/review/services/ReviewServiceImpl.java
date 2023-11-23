package dev.agasen.microsrv.core.review.services;

import java.util.List;
import java.util.logging.Level;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import dev.agasen.microsrv.core.review.persistence.ReviewEntity;
import dev.agasen.microsrv.core.review.persistence.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
@AllArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
  
  private final ServiceUtil serviceUtil;
  private final ReviewMapper reviewMapper;
  private final ReviewRepository reviewRepository;
  private final Scheduler jdbcScheduler;



  @Override
  public Flux<Review> getReviews(int productId) {

    if (productId < 1)  throw new InvalidInputException("Invalid productId: " + productId);

    log.info("Will get reviews for product with id={}", productId);

    return Mono.fromCallable(() -> internalGetReviews(productId))
        .flatMapMany(Flux::fromIterable)
        .log(log.getName(), Level.FINE)
        .subscribeOn(jdbcScheduler);
  }

  
  
  private List<Review> internalGetReviews(int productId) {
        
    List<ReviewEntity> entityList = reviewRepository.findByProductId(productId);
    List<Review> list = reviewMapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    log.debug("Response size: {}", list.size());

    return list;
  }



  @Override
  public Mono<Review> createReview(Review body) {

    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

    return Mono.fromCallable(() -> internalCreateReview(body))
        .subscribeOn(jdbcScheduler);
  }



  private Review internalCreateReview(Review body) {
    try {
      ReviewEntity entity = reviewMapper.apiToEntity(body);
      ReviewEntity newEntity = reviewRepository.save(entity);

      log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
      return reviewMapper.entityToApi(newEntity);

    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
    }
  }



  @Override
  public Mono<Void> deleteReviews(int productId) {

    return Mono.fromRunnable(() -> internalDeleteReviews(productId))
        .subscribeOn(jdbcScheduler)
        .then();
  }
  
  
  
  public void internalDeleteReviews(int productId) {

    log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
  }


}
