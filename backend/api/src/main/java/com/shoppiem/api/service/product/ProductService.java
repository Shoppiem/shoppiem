package com.shoppiem.api.service.product;

import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;

/**
 * @author Biz Melesse created on 5/25/23
 */
public interface ProductService {
  ProductCreateResponse createProduct(ProductRequest productRequest);
  String parseProductSku(String productUrl);
}
