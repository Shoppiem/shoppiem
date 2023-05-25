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
import org.hibernate.annotations.Type;

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
    @SequenceGenerator(name="pk_sequence",sequenceName="product_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="pk_sequence_product")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("product_sku")
    @Column(name = "product_sku")
    private String productSku;

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
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
