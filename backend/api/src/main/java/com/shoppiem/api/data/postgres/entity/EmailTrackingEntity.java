package com.shoppiem.api.data.postgres.entity;

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
 * created on February 18, 2023
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "email_tracking")
public class EmailTrackingEntity {

    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="email_tracking_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.AUTO,generator="pk_email_tracking")
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "num_opened")
    private Long numOpened = 1L;

    @Basic
    @Column(name = "campaign_id")
    private String campaignId;

    @Basic
    @Column(name = "ip_address")
    private String ipAddress;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
