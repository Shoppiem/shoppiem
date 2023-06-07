package com.shoppiem.api.service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.dto.ScrapingJobDto;
import com.shoppiem.api.props.ScraperProps;
import com.shoppiem.api.service.scraper.ScraperService;
import com.shoppiem.api.service.utils.JobSemaphore;
import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
  private final JobSemaphore jobSemaphore;

  public void consume(String message) throws InterruptedException {
    jobSemaphore.getSemaphore().acquire();
    handleJob(message);
  }

  private void handleJob(String message) {
    try {
      ScrapingJobDto job = objectMapper.readValue(message, ScrapingJobDto.class);
      Runnable r = null;
      if (!ObjectUtils.isEmpty(job.getProductSku())) {
        r = () -> scraperService.scrape(job.getProductSku(), job.getUrl());
      }
      if (r != null) {
        Thread.startVirtualThread(r);
      }
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

}
