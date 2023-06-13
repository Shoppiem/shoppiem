package com.shoppiem.api.service.chat;


/**
 * @author Biz Melesse created on 6/12/23
 */
public interface ChatService {

  String buildContext(String query, String productSku);
  String callGpt(String query, String productSku);

}
