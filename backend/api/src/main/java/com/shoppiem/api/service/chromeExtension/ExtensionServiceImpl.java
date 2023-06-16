package com.shoppiem.api.service.chromeExtension;

import com.shoppiem.api.ExtensionRequest;
import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.data.postgres.entity.FcmTokenEntity;
import com.shoppiem.api.data.postgres.repo.FcmTokenRepo;
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

  @Override
  public GenericResponse handleMessages(ExtensionRequest request) {
    if (request.getType().equals("HEART_BEAT")) {
      handleHeartACK();
    } else if (request.getType().equals("REGISTRATION_TOKEN")) {
      FcmTokenEntity entity = fcmTokenRepo.findByRegistrationToken(request.getToken());
      if (entity == null) {
        entity = new FcmTokenEntity();
        entity.setRegistrationToken(request.getToken());
        fcmTokenRepo.save(entity);
      }
    }
    return new GenericResponse().status("ok");
  }

  private void handleHeartACK() {
    log.info("handleHeartACK: NOT YET IMPLEMENTED");
  }
}
