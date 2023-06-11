package com.shoppiem.api.data.postgres.repo;

import com.shoppiem.api.data.postgres.entity.EmbeddingEntity;
import com.shoppiem.api.data.postgres.entity.ReviewEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 5/25/2023
 */
@Repository
@Transactional
public interface EmbeddingRepo extends JpaRepository<EmbeddingEntity, Long> {

  @Query(value = "SELECT DISTINCT ON (review_id) * " +
      "FROM embedding " +
      "WHERE review_id IN(?1)",
      nativeQuery = true)
  List<EmbeddingEntity> findAllReviewEmbeddingsByIds(List<Long> reviewIds);

  @Query(value = "SELECT * " +
      "FROM embedding " +
      "WHERE product_id = ?1 "
      + "AND review_id = -1 "
      + "AND question_id = -1 "
      + "AND answer_id = -1",
      nativeQuery = true)
  List<EmbeddingEntity> findAllProductDetailEmbeddings(Long productId);


//  Optional<ProjectEntity> findByProjectUid(String projectUid);
//  @Query(value = "SELECT * " +
//      "FROM project " +
//      "WHERE user_id = ?1 AND processed = true "
//      + "ORDER BY created_at DESC ",
//      nativeQuery = true)
//  List<ProjectEntity> findAllProjectsByUserId(String userId);
//
//  @Query(value = "SELECT * " +
//      "FROM project " +
//      "WHERE user_id = ?1 AND project_uid = ?2 AND processed = true ",
//      nativeQuery = true)
//  Optional<ProjectEntity> findProjectByUserIdAndProjectUid(String userId, String projectId);
//
//  @Query(value = "SELECT * " +
//      "FROM project " +
//      "WHERE user_id = ?1 AND processed = false "
//      + "ORDER BY created_at DESC "
//      + "LIMIT 1 ",
//      nativeQuery = true)
//  Optional<ProjectEntity> findPendingJob(String userId);
//
//  @Query(value = "SELECT "
//      + "p.project_uid as projectUid, "
//      + "p.project_name as projectName, "
//      + "p.created_at as projectCreatedAt, "
//      + "mc.permalink as contentUrl, "
//      + "u.email as email, "
//      + "u.full_name as fullName, "
//      + "u.created_at as registeredOn " +
//      "FROM public.project p "
//      + "JOIN public.user u ON u.firebase_id = p.user_id "
//      + "INNER JOIN public.media_content mc ON mc.project_id = p.id ",
//      nativeQuery = true)
//  Page<UserProjectProjection> findAllUserProjects(Pageable pageable);
}
