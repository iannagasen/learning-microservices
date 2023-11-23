package dev.agasen.microsrv.core.product.services;

import java.util.function.Consumer;

import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.EventProcessingException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MessageProcessorConfig {
  
  private final ProductService productService;



  public Consumer<Event<Integer, Product>> messageProcessor() {
    log.info("Creating messageProcessor Bean");
    return this::internalMessageProcessor;
  }



  private void internalMessageProcessor(Event<Integer, Product> event) {

    log.info("Process message created at: {}", event.getEventCreatedAt());

    switch (event.getEventType()) {
      
      case CREATE:
        Product product = event.getData();
        log.info("Create product with ID: {}", product.getProductId());
        productService.createProduct(product);
        break;

      case DELETE:
        int productId = event.getKey();
        log.info("Delete product with ProductID: {}", productId);
        productService.deleteProduct(productId).block();
        break;

      default:
        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
        log.warn(errorMessage);
        throw new EventProcessingException(errorMessage);

    }
  }
}
