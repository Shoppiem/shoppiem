package com.shoppiem.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 5/24/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "product")
public class ProductEntity {
    @Id
    @SequenceGenerator(name="product_sequence_generator",sequenceName="product_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO, generator="product_sequence_generator")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_sku")
    @Column(name = "product_sku")
    private String productSku;

    @Basic
    @Column(name = "seller")
    private String seller;

    @Basic
    @Column(name = "title")
    private String title;

    @Basic
    @Column(name = "product_url")
    private String productUrl;

    @Basic
    @Column(name = "image_url")
    private String imageUrl;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "price")
    private Double price;

    @Basic
    @Column(name = "currency")
    private String currency;

    @Basic
    @Column(name = "num_reviews")
    private Long numReviews;

    @Basic
    @Column(name = "is_ready")
    private Boolean isReady = false;

    @Basic
    @Column(name = "star_rating")
    private Double starRating;

    @Basic
    @Column(name = "num_questions_answered")
    private Long numQuestionsAnswered;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
