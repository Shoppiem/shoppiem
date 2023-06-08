package com.shoppiem.api.service;


import com.shoppiem.api.data.DataConfiguration;
import com.shoppiem.api.service.mapper.MapperConfiguration;
import com.shoppiem.api.service.messaging.Consumer;
import com.shoppiem.api.utils.migration.FlywayMigrationConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Slf4j
@Configuration
@ComponentScan
@RequiredArgsConstructor
@EnableAsync
@Import({
    RabbitMQConfiguration.class,
    WebSocketConfiguration.class,
    DataConfiguration.class,
    MapperConfiguration.class,
    FlywayMigrationConfiguration.class})
public class ServiceConfiguration {
  private final Consumer consumer;

  @Bean
  MessageListenerAdapter scrapingJobListener() {
    return new MessageListenerAdapter(consumer, "consume");
  }
}
