package com.shoppiem.api.service.messaging;

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

  public void consume(String message) {
    log.info("Message received: {}", message);
  }

}
