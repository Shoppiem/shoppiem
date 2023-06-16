package com.shoppiem.api.controller;

import com.shoppiem.api.ExtensionApi;
import com.shoppiem.api.ExtensionRequest;
import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.MessageApi;
import com.shoppiem.api.MessageRequest;
import com.shoppiem.api.service.chromeExtension.ExtensionService;
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
public class ExtensionController implements ExtensionApi {

  private final ExtensionService extensionService;

  @Override
  public ResponseEntity<GenericResponse> handleMessage(ExtensionRequest extensionRequest) {
    return ResponseEntity.ok(extensionService.handleMessages(extensionRequest));
  }
}