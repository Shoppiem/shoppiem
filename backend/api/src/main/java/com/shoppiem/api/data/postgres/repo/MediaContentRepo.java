package com.shoppiem.api.data.postgres.repo;

import com.shoppiem.api.data.postgres.entity.MediaContentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on October 20, 2022
 */
@Repository
@Transactional
public interface MediaContentRepo extends JpaRepository<MediaContentEntity, Long> {
  MediaContentEntity findByProjectId(Long projectId);
}
