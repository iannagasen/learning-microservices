package dev.agasen.microsrv.core.recommendation.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(collection="recommendations")
@CompoundIndex(name="prod-rec-id", unique=true, def="{'productId': 1, 'recommendationId': 1}") // a pair of productId and recommendationId should be unique
@NoArgsConstructor
@Getter @Setter @ToString
public class RecommendationEntity {
 
  @Id private String id;
  @Version private Integer version;

  private int productId;
  private int recommendationId;
  private String author;
  private int rating;
  private String content;
  
  public RecommendationEntity(int productId, int recommendationId, String author, int rating, String content) {
    this.productId = productId;
    this.recommendationId = recommendationId;
    this.author = author;
    this.rating = rating;
    this.content = content;
  }
}
