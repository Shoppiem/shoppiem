package com.shoppiem.api.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ScrapingJobDto;
import com.shoppiem.api.props.RabbitMQProps;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 5/25/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepo productRepo;
  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMQProps rabbitMQProps;

  @Override
  public ProductCreateResponse createProduct(ProductRequest productRequest) {
    var productSku = parseProductSku(productRequest.getProductUrl());
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null) {
      ScrapingJobDto job = new ScrapingJobDto();
      job.setProductSku(ShoppiemUtils.generateUid());
      job.setId(job.getProductSku());
      job.setUrl(productRequest.getProductUrl());
      try {
        String jobString = objectMapper.writeValueAsString(job);
        rabbitTemplate.convertAndSend(
            rabbitMQProps.getTopicExchange(),
            rabbitMQProps.getRoutingKeyPrefix() + productSku,
            jobString);
        return new ProductCreateResponse()
            .inProgress(true);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
        return new ProductCreateResponse()
            .error(e.getLocalizedMessage());
      }
    }
    if (entity.getIsReady()) {
      return new ProductCreateResponse()
          .isReady(true);
    }
    return new ProductCreateResponse()
        .inProgress(true);
  }

  @Override
  public String parseProductSku(String url) {
    // Assume we are only dealing with Amazon product URLs
    String[] tokens = url.split("/dp/");
    String skuPart = tokens[1];
    String sku = skuPart.split("/")[0];
    return sku;
  }
}
