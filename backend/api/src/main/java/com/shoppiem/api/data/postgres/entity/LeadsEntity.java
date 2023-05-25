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
 * created on February 11, 2023
 */
@Getter @Setter
@Entity
@Table(schema = "public", name = "leads")
public class LeadsEntity {

    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="leads_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.AUTO,generator="pk_leads")
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "query")
    private String query;

    @Basic
    @Column(name = "first_name")
    private String firstName;

    @Basic
    @Column(name = "last_name")
    private String lastName;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "keywords")
    private String keywords;

    @Basic
    @Column(name = "channel_url")
    private String channelUrl;

    @Basic
    @Column(name = "channel_name")
    private String channelName;

    @Basic
    @Column(name = "channel_handle")
    private String channelHandle;

    @Basic
    @Column(name = "subscribers")
    private String subscribers;

    @Basic
    @Column(name = "subscribers_value")
    private Long subscribersValue;

    @Basic
    @Column(name = "facebook")
    private String facebook;

    @Basic
    @Column(name = "instagram")
    private String instagram;

    @Basic
    @Column(name = "twitter")
    private String twitter;

    @Basic
    @Column(name = "website")
    private String website;

    @Basic
    @Column(name = "blog")
    private String blog;

    @Basic
    @Column(name = "snap_chat")
    private String snapChat;

    @Basic
    @Column(name = "discord")
    private String discord;

    @Basic
    @Column(name = "tiktok")
    private String tiktok;

    @Basic
    @Column(name = "pinterest")
    private String pinterest;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
