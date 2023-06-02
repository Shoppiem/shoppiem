package com.shoppiem.api.data.postgres.repo;


import com.shoppiem.api.data.postgres.entity.ProductAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 6/1/23
 */
@Repository
@Transactional
public interface ProductAnswerRepo extends JpaRepository<ProductAnswerEntity, Long> {
}
