package com.shoppiem.api.service;


import com.shoppiem.api.data.DataConfiguration;
import com.shoppiem.api.props.PropConfiguration;
import com.shoppiem.api.utils.TestUtilConfiguration;
import com.shoppiem.api.utils.mapper.ObjectMapperConfiguration;
import com.shoppiem.api.utils.migration.FlywayMigrationConfiguration;
import com.shoppiem.api.UserProfile;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.UUID;

/**
 * @author Bizuwork Melesse
 * created on 1/29/22
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        UserDetailsServiceAutoConfiguration.class
})
@Import({
        ServiceConfiguration.class,
        ObjectMapperConfiguration.class,
        PropConfiguration.class,
        DataConfiguration.class,
        FlywayMigrationConfiguration.class,
        TestUtilConfiguration.class
})
public class ServiceTestConfiguration {

    @Bean
    public UserProfile getUserPrincipal() {
        UserProfile principal = new UserProfile();
        principal.setUid(UUID.randomUUID().toString().replace("-", ""));
        principal.setName("Sideshow Bob");
        principal.setEmail("bobby@gmail.com");
        principal.setPicture("http://lorem.picsum/200");
        return principal;
    }
}
