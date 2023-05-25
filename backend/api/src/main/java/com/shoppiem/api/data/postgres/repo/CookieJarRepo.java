package com.shoppiem.api.data.postgres.repo;

import com.shoppiem.api.data.postgres.entity.CookieJarEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;;

/**
 * @author Bizuwork Melesse
 * created on 6/12/22
 */
@Repository
@Transactional
public interface CookieJarRepo extends JpaRepository<CookieJarEntity, Long> {

    @Query(value = "select * from cookie_jar where c_key = ?1 and c_username = ?2",
        nativeQuery = true)
    List<CookieJarEntity> findCookies(String url, String username);

    @Query(value = "select * from cookie_jar order by updated_at desc",
        nativeQuery = true)
    List<CookieJarEntity> findAll();

    @Query(value = "select * from cookie_jar where c_username = ?1 and c_key in (?2)",
        nativeQuery = true)
    List<CookieJarEntity> findCookiesByUrls(String username, List<String> urls);
}
