package com.shoppiem.api.service.product;

import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductRequest;

/**
 * @author Biz Melesse created on 5/25/23
 */
public interface ProductService {
  GenericResponse createProduct(ProductRequest productRequest);
  String parseProductSku(String productUrl);
}
