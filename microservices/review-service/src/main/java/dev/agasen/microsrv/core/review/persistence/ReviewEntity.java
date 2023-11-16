package dev.agasen.microsrv.core.review.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="reviews", indexes={
  @Index(name="reviews_unique_idx", unique=true, columnList="productId,reviewId")
})
@Getter @Setter
@AllArgsConstructor 
@NoArgsConstructor
public class ReviewEntity {

  @Id @GeneratedValue private int id;
  @Version private int version;

  private int productId;
  private int reviewId;
  private String author;
  private String subject;
  private String content;
}
