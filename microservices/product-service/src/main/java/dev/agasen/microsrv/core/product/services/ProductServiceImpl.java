package dev.agasen.microsrv.core.product.services;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import dev.agasen.microsrv.core.product.persistence.ProductEntity;
import dev.agasen.microsrv.core.product.persistence.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  public Product getProduct(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid Product Id: " + productId);
    ProductEntity entity = productRepository
        .findByProductId(productId)
        .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
    Product response = productMapper.entityToApi(entity);
    response.setServiceAddress(serviceUtil.getServiceAddress());
    log.debug("getProduct: found productId: {}", response.getProductId());
    return response;
  }

  @Override
  public Product createProduct(Product body) {
    try {
      ProductEntity entity = productMapper.apiToEntity(body);
      ProductEntity newEntity = productRepository.save(entity);
      log.debug("createProduct: entity created for productId: {}", body.getProductId());
      return productMapper.entityToApi(newEntity);
    } catch (DuplicateKeyException e) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
    }
  }

  @Override
  public void deleteProduct(int productId) {
    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
    productRepository.findByProductId(productId).ifPresent(e -> productRepository.delete(e));
  }
  
}
