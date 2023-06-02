package com.shoppiem.api.data.postgres.repo;


import com.shoppiem.api.data.postgres.entity.ProductQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 6/1/23
 */
@Repository
@Transactional
public interface ProductQuestionRepo extends JpaRepository<ProductQuestionEntity, Long> {
  ProductQuestionEntity findByQuestionId(String questionId);
}
