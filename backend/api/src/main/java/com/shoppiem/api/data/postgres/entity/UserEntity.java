package com.shoppiem.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "user")
public class UserEntity {
    @Id
    @SequenceGenerator(name="user_sequence_generator",sequenceName="user_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.AUTO,generator="user_sequence_generator")
    @Column(name = "id")
    private Long id;
    
    @JsonProperty("uid")
    @Column(name = "uid")
    private String uid;

    @Basic
    @JsonProperty("name")
    @Column(name = "name")
    private String name;

    @Basic
    @JsonProperty("is_premium_user")
    @Column(name = "is_premium_user")
    private Boolean isPremiumUser = false;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
