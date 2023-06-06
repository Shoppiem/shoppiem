package com.shoppiem.api.service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.dto.ScrapingJobDto;
import com.shoppiem.api.service.scraper.ScraperService;
import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 6/4/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Consumer {
  private final ObjectMapper objectMapper;
  private final ScraperService scraperService;

  /**
   * The number of concurrent scraping requests must not exceed a certain
   * number. This number depends on the subscription plan we have for proxy
   * rotation and how many scraping threads they recommend. We need to block
   * the consumer until a slot becomes available.
   */
  public static volatile AtomicInteger scrapingJobsInProgress = new AtomicInteger(0);

  private final Executor executor = Executors.newThreadPerTaskExecutor(
      Thread.ofVirtual().name("routine-", 0).factory());

  public void consume(String message) {
    while (scrapingJobsInProgress.get() >= 10) {
      try {
        Thread.sleep(10L);
      } catch (InterruptedException e) {
        log.error(e.getLocalizedMessage());
      }
    }
    scrapingJobsInProgress.incrementAndGet();
    handleJob(message);
  }

  private void handleJob(String message) {
    try {
      ScrapingJobDto job = objectMapper.readValue(message, ScrapingJobDto.class);
      Runnable r = null;
      var factory = Thread.ofVirtual().name("routine-", 0).factory();
      if (!ObjectUtils.isEmpty(job.getProductSku())) {
        r = () -> scraperService.scrape(job.getProductSku(), job.getUrl());
      }
      if (r != null) {
        try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
          var task = executor.submit(r);
          try {
            task.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        }
      }
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

}
