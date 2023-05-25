package com.shoppiem.api.data.postgres.repo;


import com.shoppiem.api.data.postgres.entity.EmailCampaignEntity;
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
public interface EmailCampaignRepo extends JpaRepository<EmailCampaignEntity, Long> {
    @Query(value = "SELECT * " +
        "FROM email_campaign " +
        "ORDER BY created_at DESC",
        nativeQuery = true)
    List<EmailCampaignEntity> findAllOrderByCreatedAtDesc();
}
