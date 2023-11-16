package dev.agasen.microsrv.core.recommendation.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection="recommendations")
@CompoundIndex(name="prod-rec-id", unique=true, def="{'productId': 1, 'recommendationId': 1}") // a pair of productId and recommendationId should be unique
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class RecommendationEntity {
 
  @Id private String id;
  @Version private Integer version;

  private int productId;
  private int recommendation;
  private String author;
  private int rating;
  private String content;
}
