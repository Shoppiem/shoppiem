package com.shoppiem.api.service.chat;


import com.shoppiem.api.service.openai.completion.CompletionRequest;

/**
 * @author Biz Melesse created on 6/12/23
 */
public interface ChatService {

  CompletionRequest buildGptRequest(String query, String productSku);
  void callGpt(String query, String registrationToken, String productSku);
  void addQueryToQueue(String query, String fcmToken, String productSku);

}
