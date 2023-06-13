package com.shoppiem.api.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductFromDataRequest;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ScrapingJobDto;
import com.shoppiem.api.dto.ScrapingJobDto.JobType;
import com.shoppiem.api.props.RabbitMQProps;
import com.shoppiem.api.service.embedding.EmbeddingService;
import com.shoppiem.api.service.parser.AmazonParser;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import java.net.MalformedURLException;
import java.net.URL;
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
  private final AmazonParser amazonParser;
  private final EmbeddingService embeddingService;

  @Override
  public ProductCreateResponse createProductFromData(
      ProductFromDataRequest productFromDataRequest, boolean schedule) {
    var url = cleanupUrl(productFromDataRequest.getProductUrl());
    var productSku = parseProductSku(url);
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null) {
      entity = new ProductEntity();
      entity.setProductSku(productSku);
      entity.setNumReviews(productFromDataRequest.getNumReviews());
      entity.setStarRating(productFromDataRequest.getStarRating());
      entity.setNumQuestionsAnswered(productFromDataRequest.getNumQuestionsAnswered());
      entity.setCurrency(productFromDataRequest.getCurrency());
      entity.setImageUrl(productFromDataRequest.getImageUrl());
      entity.setPrice(productFromDataRequest.getPrice());
      entity.setProductUrl(url);
      entity.setSeller(productFromDataRequest.getSeller());
      entity.setTitle(ShoppiemUtils.truncate(productFromDataRequest.getTitle()));
      entity.setDescription(productFromDataRequest.getDescription());
      entity.setIsReady(false);
      productRepo.save(entity);
      if (schedule) {
        final ProductEntity finalEntity = entity;
        Thread.startVirtualThread(() -> embeddingService.embedProduct(finalEntity));
        scheduleJobs(entity);
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
  public ProductCreateResponse createProduct(ProductRequest productRequest) {
    var url = cleanupUrl(productRequest.getProductUrl());
    var productSku = parseProductSku(url);
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null) {
      entity = new ProductEntity();
      entity.setProductSku(productSku);
      productRepo.save(entity);
      ScrapingJobDto job = new ScrapingJobDto();
      job.setProductSku(productSku);
      job.setId(ShoppiemUtils.generateUid());
      job.setUrl(url);
      job.setType(JobType.PRODUCT_PAGE);
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

  private String cleanupUrl(String url) {
    try {
      URL parsedUrl = new URL(url);
      return String.format("%s://%s%s", parsedUrl.getProtocol(), parsedUrl.getHost(), parsedUrl.getPath());
    } catch (MalformedURLException e) {
      return url;
    }
  }

  @Override
  public String parseProductSku(String url) {
    // Assume we are only dealing with Amazon product URLs
    String[] tokens = url.split("/dp/");
    String skuPart = tokens[1];
    String sku = skuPart.split("/")[0];
    return sku;
  }

  @Override
  public void scheduleJobs(ProductEntity productEntity) {
    Thread.startVirtualThread(() -> amazonParser.scheduleQandAScraping(productEntity));
    Thread.startVirtualThread(() -> amazonParser.scheduleInitialReviewScraping(productEntity));
  }
}
