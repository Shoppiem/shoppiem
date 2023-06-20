package com.shoppiem.api.service.product;

import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductFromDataRequest;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.data.postgres.entity.ProductEntity;

/**
 * @author Biz Melesse created on 5/25/23
 */
public interface ProductService {
  ProductCreateResponse createProductFromData(ProductFromDataRequest productFromDataRequest,
      boolean scheduleJobs);
  ProductCreateResponse createProduct(ProductRequest productRequest, String fcmToken);

  void sendProductInfoToClient(ProductEntity entity, String productSku, String fcmToken);
  String parseProductSku(String productUrl);

  void scheduleJobs(ProductEntity productEntity);
}
