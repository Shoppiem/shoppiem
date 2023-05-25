package com.shoppiem.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 5/25/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "review")
public class ReviewEntity {
    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="review_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="pk_sequence_review")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @Column(name = "title")
    private String title;

    @Basic
    @Column(name = "review_id")
    private String reviewId;

    @Basic
    @Column(name = "merchant")
    private String merchant;

    @Basic
    @Column(name = "location")
    private String location;

    @Basic
    @Column(name = "verified_purchase")
    private Boolean verifiedPurchase = false;

    @Basic
    @Column(name = "likes")
    private Long likes;

    @Basic
    @Column(name = "star_rating")
    private Long starRating;

    @Basic
    @Column(name = "reviewer_name")
    private String reviewerName;

    @Basic
    @Column(name = "reviewer_handle")
    private String reviewerHandle;

    @Basic
    @Column(name = "body")
    private String body;

    @Basic
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
