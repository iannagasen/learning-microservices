package dev.agasen.microsrv.core.review.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import dev.agasen.microsrv.core.review.persistence.ReviewEntity;
import dev.agasen.microsrv.core.review.persistence.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public List<Review> getReviews(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    if (productId == 213) {
      log.debug("No reviews found for productId: {}", productId);
      return new ArrayList<>();
    }

    List<Review> list = new ArrayList<>();
    list.add(new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
    list.add(new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
    list.add(new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));

    log.debug("/reviews response size: {}", list.size());

    return list;    

  }

  @Override
  public Review createReview(Review body) {
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
  public void deleteReviews(int productId) {
    log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
  }
}
