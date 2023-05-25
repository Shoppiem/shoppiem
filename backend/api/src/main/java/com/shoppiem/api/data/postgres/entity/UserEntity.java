package com.shoppiem.api.data.postgres.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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
    @JsonProperty("firebase_id")
    @Column(name = "firebase_id")
    private String firebaseId;

    @Basic
    @JsonProperty("full_name")
    @Column(name = "full_name")
    private String fullName;

    @Basic
    @JsonProperty("first_name")
    @Column(name = "first_name")
    private String firstName;

    @Basic
    @JsonProperty("last_name")
    @Column(name = "last_name")
    private String lastName;

    @Basic
    @JsonProperty("is_premium_user")
    @Column(name = "is_premium_user")
    private Boolean isPremiumUser = false;

    @Basic
    @Column(name = "email")
    private String email;

    @Type(type = "com.shoppiem.api.data.type.ArrayUserType")
    @Column(name = "roles", columnDefinition = "text[]")
    Object[] roles;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
