package com.shoppiem.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductApi;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.dto.ScrapingJobDto;
import com.shoppiem.api.service.product.ProductService;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import java.util.UUID;
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
  private final ObjectMapper objectMapper;

  private final RabbitTemplate rabbitTemplate;
  private String topicExchangeName = "shoppiem-exchange";

  @Override
  public ResponseEntity<ProductCreateResponse> createProduct(ProductRequest productRequest) {
    return ResponseEntity.ok(productService.createProduct(productRequest));
  }

  @Override
  public ResponseEntity<GenericResponse> testRabbitMQ(Integer count) {

    for (int i = 0; i < count; i++) {
      ScrapingJobDto job = new ScrapingJobDto();
      job.setProductSku(ShoppiemUtils.generateUid());
      job.setUrl("http://example.com/" + i);
      try {
        String jobString = objectMapper.writeValueAsString(job);
        rabbitTemplate.convertAndSend(topicExchangeName, "shoppiem.job.job_id_" + i, jobString);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return ResponseEntity.ok(new GenericResponse().status("OK"));
  }
}
