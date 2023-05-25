package com.shoppiem.api.data.postgres.repo;


import com.shoppiem.api.data.postgres.entity.LeadsEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on February 11, 2023
 */
@Repository
@Transactional
public interface LeadsRepo extends JpaRepository<LeadsEntity, Long> {
    List<LeadsEntity> findByFacebook(String url);
}
