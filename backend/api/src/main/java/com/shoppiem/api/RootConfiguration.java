package com.shoppiem.api;

import com.shoppiem.api.controller.ControllerConfiguration;
import com.shoppiem.api.data.DataConfiguration;
import com.shoppiem.api.props.PropConfiguration;
import com.shoppiem.api.service.ServiceConfiguration;
import com.shoppiem.api.utils.migration.FlywayMigration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Slf4j
@Configuration
@Import({
        ControllerConfiguration.class,
        DataConfiguration.class,
        PropConfiguration.class,
        ServiceConfiguration.class
})
@RequiredArgsConstructor
public class RootConfiguration {
  private final FlywayMigration flywayMigration;

  @PostConstruct
  public void onStart() {
    flywayMigration.migrate(false);
  }
}
