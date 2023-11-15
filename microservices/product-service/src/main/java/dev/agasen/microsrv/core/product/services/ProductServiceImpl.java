package dev.agasen.microsrv.core.product.services;

import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.util.http.ServiceUtil;

@RestController
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ServiceUtil serviceUtil;

  @Override
  public Product getProduct(int productId) {
    return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());    
  }
  
}
