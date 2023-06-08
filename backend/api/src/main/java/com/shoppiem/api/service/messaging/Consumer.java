package com.shoppiem.api.service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.dto.ScrapingJobDto;
import com.shoppiem.api.dto.ScrapingJobDto.JobType;
import com.shoppiem.api.service.scraper.ScraperService;
import com.shoppiem.api.service.utils.JobSemaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    try {
      ScrapingJobDto job = objectMapper.readValue(message, ScrapingJobDto.class);
      Thread.startVirtualThread(() ->
          scraperService.scrape(job.getProductSku(), job.getUrl(), JobType.PRODUCT_PAGE));
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }
}
