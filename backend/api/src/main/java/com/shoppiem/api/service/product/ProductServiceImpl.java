package com.shoppiem.api.service.product;

import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 5/25/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepo productRepo;

  @Override
  public GenericResponse createProduct(ProductRequest productRequest) {
    var productSku = parseProductSku(productRequest.getProductUrl());
    var entity = productRepo.findByProductSku(productSku);
    if (entity == null) {
      // Product not found. Scrape this product and its reviews.
      // And then create embeddings for it.
    } else {
      // Signal that the product is ready and can start the chat session
    }
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
