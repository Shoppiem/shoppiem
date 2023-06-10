package com.shoppiem.api.service.utils;

import com.shoppiem.api.props.ScraperProps;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Biz Melesse created on 6/6/23
 */
@Component
@RequiredArgsConstructor
public class JobSemaphore {

  /**
   * The number of concurrent scraping requests must not exceed a certain
   * number. This number depends on the subscription plan we have for proxy
   * rotation and how many scraping threads they recommend. We need to block
   * the consumer until a slot becomes available.
   */
  private final ScraperProps scraperProps;

  private Semaphore semaphore = null;


  public Semaphore getSemaphore() {
    if (semaphore == null) {
      semaphore = new Semaphore(10);
    }
    return semaphore;
  }

}
