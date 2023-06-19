package com.shoppiem.api.service.chromeExtension;

import com.shoppiem.api.ExtensionRequest;
import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.data.postgres.entity.FcmTokenEntity;
import com.shoppiem.api.data.postgres.repo.FcmTokenRepo;
import com.shoppiem.api.service.chat.ChatService;
import com.shoppiem.api.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 6/15/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtensionServiceImpl implements ExtensionService {
  private final FcmTokenRepo fcmTokenRepo;
  private final ProductService productService;
  private final ChatService chatService;

  public static final class MessageType {
    public static final String CHAT = "CHAT";
    public static final String HEART_BEAT = "HEART_BEAT";
    public static final String PRODUCT_INIT = "PRODUCT_INIT";
    public static final String FCM_TOKEN = "FCM_TOKEN";
  }

  @Override
  public GenericResponse handleMessages(ExtensionRequest request) {
    switch (request.getType()) {
      case MessageType.HEART_BEAT -> handleHeartbeatACK();
      case MessageType.FCM_TOKEN ->  {
        FcmTokenEntity entity = fcmTokenRepo.findByFcmToken(request.getToken());
        if (entity == null) {
          entity = new FcmTokenEntity();
          entity.setFcmToken(request.getToken());
          fcmTokenRepo.save(entity);
        }
      }
      case MessageType.CHAT -> chatService.addQueryToQueue(
          request.getQuery(), request.getToken(), request.getProductSku(), 0, false);
      case MessageType.PRODUCT_INIT -> productService.createProduct(new ProductRequest()
          .productUrl(request.getProductUrl())
          .html(request.getHtml()));
    }
    return new GenericResponse().status("ok");
  }

  private void handleHeartbeatACK() {
    log.info("handleHeartACK: NOT YET IMPLEMENTED");
  }
}
