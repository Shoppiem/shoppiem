package com.shoppiem.api.controller;

import com.shoppiem.api.ProductApi;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public ResponseEntity<ProductCreateResponse> createProduct(ProductRequest productRequest) {
    return ResponseEntity.ok(productService.createProduct(productRequest));
  }
}