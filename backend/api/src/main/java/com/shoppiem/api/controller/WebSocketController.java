package com.shoppiem.api.controller;

import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse created on 05/28/23
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class WebSocketController {
  private final ProductService productService;


  @MessageMapping("/create-product")
  public void processMessage(@Payload ProductRequest productRequest) {
    productService.createProduct(productRequest, null);
  }
}
