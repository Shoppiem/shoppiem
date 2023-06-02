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
 * created on 6/1/23
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "product_answer")
public class ProductAnswerEntity {
    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="product_answer_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO, generator="pk_sequence_product_answer")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_id")
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @JsonProperty("product_question_id")
    @Column(name = "product_question_id")
    private String productQuestionId;

    @Basic
    @Column(name = "answer")
    private String answer;

    @Basic
    @Column(name = "answered_by_handle")
    private String answeredByHandle;

    @Basic
    @Column(name = "answered_by_url")
    private String answeredByUrl;

    @Basic
    @Column(name = "upvotes")
    private Long upvotes;

    @Basic
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
