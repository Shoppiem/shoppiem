package com.shoppiem.api.controller;

import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductApi;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse
 * created on 5/25/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {
  private final ProductService productService;

  private final RabbitTemplate rabbitTemplate;
  private String topicExchangeName = "shoppiem-exchange";

  @Override
  public ResponseEntity<ProductCreateResponse> createProduct(ProductRequest productRequest) {
    return ResponseEntity.ok(productService.createProduct(productRequest));
  }

  @Override
  public ResponseEntity<GenericResponse> testRabbitMQ(String message) {

    int count = 1000000;
    for (int i = 0; i < count; i++) {
      rabbitTemplate.convertAndSend(topicExchangeName, "shoppiem.job.job_id_" + i, message + "_" + i);
    }
    return ResponseEntity.ok(new GenericResponse().status(message));
  }
}
