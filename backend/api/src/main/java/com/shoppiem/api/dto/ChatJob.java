package com.shoppiem.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 6/5/23
 */
@Getter @Setter
public class ChatJob {
  private String query;
  private String productSku;
  private String registrationToken;
  private String id;
}
