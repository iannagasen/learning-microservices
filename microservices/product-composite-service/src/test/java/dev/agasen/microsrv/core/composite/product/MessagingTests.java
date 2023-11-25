package dev.agasen.microsrv.core.composite.product;

import static dev.agasen.microsrv.core.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.event.Event;
import se.magnus.api.event.Event.Type;


@SpringBootTest(
  // it will start up the application at a random port
  webEnvironment = RANDOM_PORT,
  properties = {
    // I think we add this here, so that the @Import below works
    "spring.main.allow-bean-definition-overriding=true",
    
    // we dont want to depend on having Eureka Server up and running
    "eureka.client.enabled=false"
  })
@Import({ TestChannelBinderConfiguration.class })
public class MessagingTests {

  private static final Logger log = LoggerFactory.getLogger(MessagingTests.class);
  
  @Autowired private WebTestClient client;
  @Autowired private OutputDestination target;

  // bindingNames
  static final String PRODUCT = "products";
  static final String RECOMMENDATION = "recommendations";
  static final String REVIEW = "reviews";



  @BeforeEach
  void setup() {
    purgeMessages(PRODUCT);
    purgeMessages(RECOMMENDATION);
    purgeMessages(REVIEW);
  }



  @Test
  void testCreateComposite_withProduct_butNoReviews_andNoRecommendations() {

    log.info("MessagingTests::testCreateComposite_withProduct_butNoReviews_andNoRecommendations");

    ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);
    postAndVerifyProduct(composite, ACCEPTED);

    final List<String> productMessages = getMessages(PRODUCT);
    final List<String> recommendationMessages = getMessages(RECOMMENDATION);
    final List<String> reviewMessages = getMessages(REVIEW);

    Product product = new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null);

    // Assert one expected new product event queued up
    assertEquals(1, productMessages.size());
    assertThat(
      productMessages.get(0), 
      is(sameEventExceptCreatedAt(new Event<>(Type.CREATE, composite.getProductId(), product)))
    );

    // assert no recommendation and review events. Why? because in the composite, recommendations and reviews are null
    assertEquals(0, recommendationMessages.size());
    assertEquals(0, reviewMessages.size());
  }



  @Test
  void deleteCompositeProduct() {
    deleteAndVerifyProduct(1, ACCEPTED);

    final List<String> productMessages = getMessages("products");
    final List<String> recommendationMessages = getMessages("recommendations");
    final List<String> reviewMessages = getMessages("reviews");

    // Assert one delete product event queued up
    assertEquals(1, productMessages.size());

    Event<Integer, Product> expectedProductEvent = new Event(Type.DELETE, 1, null);
    assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

    // Assert one delete recommendation event queued up
    assertEquals(1, recommendationMessages.size());

    Event<Integer, Product> expectedRecommendationEvent = new Event(Type.DELETE, 1, null);
    assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

    // Assert one delete review event queued up
    assertEquals(1, reviewMessages.size());

    Event<Integer, Product> expectedReviewEvent = new Event(Type.DELETE, 1, null);
    assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
  }



  @Test
  void testCreateComposite_withProduct_withReviews_withRecommendations() {
    
    log.info("MessagingTests::testCreateComposite_withProduct_withReviews_withRecommendations");

    RecommendationSummary rec = new RecommendationSummary(1, "a", 1, "c");
    ReviewSummary rev = new ReviewSummary(1, "a", "s", "c");

    ProductAggregate composite = new ProductAggregate(1, "name", 1,
      Collections.singletonList(rec),
      Collections.singletonList(rev), null);

    postAndVerifyProduct(composite, ACCEPTED);

    final List<String> productMessages = getMessages(PRODUCT);
    final List<String> recommendationMessages = getMessages(RECOMMENDATION);
    final List<String> reviewMessages = getMessages(REVIEW);

    Product product = new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null);
    Recommendation recommendation = new Recommendation(composite.getProductId(), rec.getRecommendationId(), rec.getAuthor(), rec.getRate(), rec.getContent(), null);
    Review review = new Review(composite.getProductId(), rev.getReviewId(), rev.getAuthor(), rev.getSubject(), rev.getContent(), null);

    // Assert one expected new product event queued up
    assertEquals(1, productMessages.size());
    assertThat(
      productMessages.get(0), 
      is(sameEventExceptCreatedAt(new Event<>(Type.CREATE, composite.getProductId(), product)))
    );

    // Assert one create recommendation event queued up
    assertEquals(1, composite.getRecommendations().size());
    assertThat(
      recommendationMessages.get(0), 
      is(sameEventExceptCreatedAt(new Event<>(Type.CREATE, composite.getProductId(), recommendation)))
    );

    // Assert one create review event queued up
    assertEquals(1, composite.getReviews().size());
    assertThat(
      reviewMessages.get(0), 
      is(sameEventExceptCreatedAt(new Event<>(Type.CREATE, composite.getProductId(), review)))  
    );
  }



  private void purgeMessages(String bindingName) {
    // this was purge bc, everytime it is read, it will be deleted
    getMessages(bindingName);
  }



  private List<String> getMessages(String bindingName) {
    List<String> messages = new ArrayList<>();
    boolean hasStillMessages = true;

    while (hasStillMessages) {
      Message<byte[]> message = getMessage(bindingName);

      if (message == null) {
        hasStillMessages = false; // exit the loop
      } else {
        messages.add(new String(message.getPayload()));
      }
    }

    return messages;
  }


  
  private Message<byte[]> getMessage(String bindingName) {
    try {
      return target.receive(0, bindingName);
    } catch (NullPointerException e) {
      // If the messageQueues member variable in the target object contains no queues when the receive method is called, it will cause a NPE to be thrown.
      // So we catch the NPE here and return null to indicate that no messages were found.
      log.error("getMessage() received a NPE with binding = {}", bindingName);
      return null;
    }
  }



  private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
    client.post()
      .uri("/product-composite")
      .body(Mono.just(compositeProduct), ProductAggregate.class)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus);
  }



  private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    client.delete()
      .uri("/product-composite/" + productId)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus);
  }
  
}
