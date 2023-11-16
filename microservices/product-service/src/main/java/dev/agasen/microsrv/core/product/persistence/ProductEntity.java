package dev.agasen.microsrv.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductEntity {
  
  @Id private String ld;
  @Version private Integer version;
  @Indexed private int productId; // create a unique index key for this business key

  private String name;
  private int weight;
}
