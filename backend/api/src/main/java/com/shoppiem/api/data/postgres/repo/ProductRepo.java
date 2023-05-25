package com.shoppiem.api.data.postgres.repo;


import com.shoppiem.api.data.postgres.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on 4/21/22
 */
@Repository
@Transactional
public interface ProductRepo extends JpaRepository<ProductEntity, Long> {
  ProductEntity findByProductSku(String sku);
}
