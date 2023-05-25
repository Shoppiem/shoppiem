package com.shoppiem.api.data.postgres.entity;

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
@Table(schema = "public", name = "project")
public class ProjectEntity {

    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="project_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.AUTO,generator="pk_sequence_project")
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "project_uid")
    private String projectUid;

    @Basic
    @Column(name = "user_id")
    private String userId;

    @Basic
    @Column(name = "project_name")
    private String projectName = "Untitled";

    @Basic
    @Column(name = "content_link")
    private String contentLink;

    @Basic
    @Column(name = "paraphrase")
    private Boolean paraphrase = true;

    @Basic
    @Column(name = "get_raw_transcript")
    private Boolean getRawTranscript = false;

    @Basic
    @Column(name = "embed_images")
    private Boolean embedImages = false;

    @Basic
    @Column(name = "processed")
    private Boolean processed = false;

    @Basic
    @Column(name = "failed")
    private Boolean failed = false;

    @Basic
    @Column(name = "content_type")
    private String contentType;

    @Basic
    @Column(name = "reason_failed")
    private String reasonFailed;

    @Basic
    @Column(name = "content")
    private String content;

    @Basic
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
