package com.shoppiem.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Bizuwork Melesse
 * created on October 20, 2022
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "sampled_image")
public class SampledImageEntity {

    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="sampled_image_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.AUTO,generator="pk_sequence_sampled_image")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("project_id")
    @Column(name = "project_id")
    private Long projectId;

    @Basic
    @JsonProperty("image_key")
    @Column(name = "image_key")
    private String imageKey;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
