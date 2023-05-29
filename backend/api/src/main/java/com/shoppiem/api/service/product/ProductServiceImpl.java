package com.shoppiem.api.service.product;

import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.props.WebSocketProps;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 5/25/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepo productRepo;
  private final WebSocketProps webSocketProps;
  private final SimpMessagingTemplate messagingTemplate;

  @SneakyThrows
  @Override
  public GenericResponse createProduct(ProductRequest productRequest) {
    var productSku = parseProductSku(productRequest.getProductUrl());
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null) {
      // Product not found. Scrape this product and its reviews.
      // And then create embeddings for it.
      messagingTemplate.convertAndSendToUser(
          productRequest.getSessionId(),
          webSocketProps.getQueue(),
          new ProductCreateResponse()
              .inProgress(true)
      );
    } else {
      // Signal that the product is ready and can start the chat session
    }
    Thread.sleep(5000L);
    messagingTemplate.convertAndSendToUser(
        productRequest.getSessionId(),
        webSocketProps.getQueue(),
        new ProductCreateResponse()
            .isReady(true)
    );
    return null;
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
