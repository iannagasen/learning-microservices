package dev.agasen.microsrv.core.product.services;

import java.util.logging.Level;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import dev.agasen.microsrv.core.product.persistence.ProductEntity;
import dev.agasen.microsrv.core.product.persistence.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
@AllArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ServiceUtil serviceUtil;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;



  @Override
  public Mono<Product> getProduct(int productId) {

    if (productId < 1) throw new InvalidInputException("Invalid Product Id: " + productId);
    
    log.info("Will get product info for id={}", productId);

    return productRepository
        .findByProductId(productId)
        .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId))) // simmilar to Optional::orElseThrow
        .log(log.getName(), Level.FINE)
        .map(productMapper::entityToApi)
        .map(this::setServiceAddress);
  }



  @Override
  public Mono<Product> createProduct(Product body) {

    log.info("Creating product with product id: {}", body.getProductId());

    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

    ProductEntity entity = productMapper.apiToEntity(body);
    return productRepository
        .save(entity)
        .log(log.getName(), Level.FINE)
        .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
        .map(productMapper::entityToApi);
  }



  @Override
  public Mono<Void> deleteProduct(int productId) {

    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

    return productRepository
        .findByProductId(productId)
        .log(log.getName(), Level.FINE)
        .map(productRepository::delete)
        .flatMap(e -> e);
  }
  


  private Product setServiceAddress(Product e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
  
}
