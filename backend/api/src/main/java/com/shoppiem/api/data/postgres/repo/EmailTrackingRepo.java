package com.shoppiem.api.data.postgres.repo;


import com.shoppiem.api.data.postgres.entity.EmailTrackingEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Bizuwork Melesse
 * created on February 18, 2023
 */
@Repository
@Transactional
public interface EmailTrackingRepo extends JpaRepository<EmailTrackingEntity, Long> {
    List<EmailTrackingEntity> findByEmailAndCampaignId(String email, String campaignId);

    @Query(value = "SELECT * "
        + "FROM email_tracking "
        + "WHERE num_opened >= ?1 "
        + "ORDER BY updated_at DESC",
        nativeQuery = true)
    List<EmailTrackingEntity> findAllOrderByUpdatedAtDesc(Long minOpenCount);
}
