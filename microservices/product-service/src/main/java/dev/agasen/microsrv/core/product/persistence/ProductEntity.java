package dev.agasen.microsrv.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "products")
@NoArgsConstructor
@Getter @Setter
public class ProductEntity {
  
  @Id private String id;
  @Version private Integer version;

  @Indexed private int productId; // create a unique index key for this business key
  private String name;
  private int weight;

  public ProductEntity(int productId, String name, int weight) {
    this.productId = productId;
    this.name = name;
    this.weight = weight;
  }
}
