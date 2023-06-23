package com.shoppiem.api.service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.dto.ChatJob;
import com.shoppiem.api.dto.ScrapingJob;
import com.shoppiem.api.service.chat.ChatService;
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
  private final ChatService chatService;
  private final JobSemaphore jobSemaphore;
  private final int numRetries = 3;

  public void scrapeJobConsumer(String message) throws InterruptedException {
    jobSemaphore.getScrapeJobSemaphore().acquire();
    try {
      ScrapingJob job = objectMapper.readValue(message, ScrapingJob.class);
      Thread.startVirtualThread(() ->
          scraperService.scrape(job.getId(), job.getProductSku(), job.getUrl(), job.getType(), true,
              Math.max(numRetries, job.getRetries()), true,
              job.isInitialReviewsByStarRating(), job.getStarRating()));
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private void chatJobConsumer(String message) throws InterruptedException {
    jobSemaphore.getChatJobSemaphore().acquire();
    try {
      ChatJob job = objectMapper.readValue(message, ChatJob.class);
      Thread.startVirtualThread(() ->
          chatService.callGpt(job.getQuery(), job.getFcmToken(), job.getProductSku()));
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }
}
