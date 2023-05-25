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
@Table(schema = "public", name = "media_content")
public class MediaContentEntity {

    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="media_content_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.AUTO,generator="pk_sequence_media_content")
    @Column(name = "id")
    private Long id;

    @Basic
    @JsonProperty("project_id")
    @Column(name = "project_id")
    private Long projectId;

    @Basic
    @JsonProperty("media_id")
    @Column(name = "media_id")
    private String mediaId;

    @Basic
    @JsonProperty("scraped_title")
    @Column(name = "scraped_title")
    private String scrapedTitle;
    
    @Basic
    @JsonProperty("scraped_description")
    @Column(name = "scraped_description")
    private String scrapedDescription;
    
    @Basic
    @JsonProperty("raw_transcript")
    @Column(name = "raw_transcript")
    private String rawTranscript;

    @Basic
    @Column(name = "permalink")
    private String permalink;

    @Basic
    @JsonProperty("on_screen_text")
    @Column(name = "on_screen_text")
    private String onScreenText;
    
    @Basic
    @JsonProperty("creator_handle")
    @Column(name = "creator_handle")
    private String creatorHandle;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
