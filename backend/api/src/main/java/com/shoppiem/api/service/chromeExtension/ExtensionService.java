package com.shoppiem.api.service.chromeExtension;

import com.shoppiem.api.ExtensionRequest;
import com.shoppiem.api.GenericResponse;

/**
 * @author Biz Melesse created on 6/15/23
 */
public interface ExtensionService {
  GenericResponse handleMessages(ExtensionRequest request);
}
