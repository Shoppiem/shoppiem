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
    handleJob(message);
  }

  private void handleJob(String message) {
    try {
      ScrapingJobDto job = objectMapper.readValue(message, ScrapingJobDto.class);
      JobType type = job.getType();
      switch (type) {
        case PRODUCT_PAGE -> Thread.startVirtualThread(
            () -> scraperService.scrape(job.getProductSku(), job.getUrl()));
        case REVIEW_PAGE -> {}
        case ANSWER_PAGE -> {}
        case QUESTION_PAGE -> {}
        case default -> {}
      }
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

}
