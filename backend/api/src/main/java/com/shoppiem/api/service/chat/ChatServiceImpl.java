package com.shoppiem.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.shoppiem.api.data.postgres.entity.ChatHistoryEntity;
import com.shoppiem.api.data.postgres.repo.ChatHistoryRepo;
import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.service.embedding.EmbeddingService;
import com.shoppiem.api.service.openai.completion.CompletionRequest;
import com.shoppiem.api.service.openai.completion.CompletionResult;
import com.shoppiem.api.service.openai.completion.CompletionMessage;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 6/12/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
  private final EmbeddingService embeddingService;
  private final ObjectMapper objectMapper;
  private final OpenAiProps openAiProps;
  private final ChatHistoryRepo chatHistoryRepo;

  @Override
  public CompletionRequest buildGptRequest(String query, String productSku) {
    List<String> embeddings = embeddingService.fetchEmbeddings(query, productSku);
    String context = String.join(" ", embeddings);
    String content = String.format("CONTEXT:\n%s\n\nQUESTION: %s\n\nANSWER:",
        context, query);
    return CompletionRequest
        .builder()
        .user(productSku)
        .model(openAiProps.getCompletionModel())
        .maxTokens(openAiProps.getMaxTokens())
        .temperature(openAiProps.getTemp())
        .messages(List.of(
            new CompletionMessage("system", openAiProps.getSystemMessage()),
            new CompletionMessage("user", content)
        )).build();
  }

  @Override
  public void callGpt(String query, String productSku) {
    Thread.startVirtualThread(() -> saveToChatHistory(query, productSku, false));
//    sendFcmMessage(LocalDateTime.now().toString());
    CompletionRequest request = buildGptRequest(query, productSku);
    try {
      String json = objectMapper.writeValueAsString(request);
      CompletionResult result = gptHttpRequest(json);
      if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
          result.getChoices().get(0).getMessage() != null) {
        String response = result.getChoices().get(0).getMessage().getContent();
        Thread.startVirtualThread(() -> saveToChatHistory(response, productSku, true));
        sendFcmMessage(response);
        log.info("Assistant: {}", response);
      }
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private void saveToChatHistory(String query, String productSku, boolean isGpt) {
    ChatHistoryEntity entity = new ChatHistoryEntity();
    entity.setMessage(query);
    entity.setIsGpt(isGpt);
    entity.setProductSku(productSku);
    entity.setChatId(ShoppiemUtils.generateUid(16));

    /***
     * TODO: use the real user id
     */
    entity.setUserId(-1L);
  }

  private void sendFcmMessage(String chatMessage) {
    String registrationToken = "APA91bE4sRfKX9swQU3g91_gFgeQciZg6-mP1V1SAdDBPjfbyg8tetPzmL9GZUQMfSjY3Lz2xspDbo9BAYRjw1M4qzqLsVVeeaGhyoVXPorZ9loQrdR0K6tQIFxOuSVM3GvqSCLTZ3FNLwVEYm12dBb4ZltDCLlAug";

// See documentation on defining a message payload.
    Message message = Message.builder()
        .putData("content", chatMessage)
        .putData("greeting", "Hello, world!")
        .putData("sender", "shoppiem-server")
        .setToken(registrationToken)
        .build();

// Send a message to the device corresponding to the provided
// registration token.
    String response = null;
    try {
      response = FirebaseMessaging.getInstance().send(message);
      // Response is a message ID string.
      System.out.println("Successfully sent message: " + response);
    } catch (FirebaseMessagingException e) {
      throw new RuntimeException(e);
    }

  }

  private CompletionResult gptHttpRequest(String json) {
    OkHttpClient httpClient = new OkHttpClient.Builder().build();
    RequestBody body = RequestBody.create(
        MediaType.parse("application/json"), json);
    Request request = new Request.Builder()
        .url(openAiProps.getCompletionEndpoint())
        .addHeader("Authorization", "Bearer " + openAiProps.getApiKey())
        .post(body)
        .build();
    Call call = httpClient.newCall(request);
    try {
      Response response = call.execute();
      String r = response.body().string();
      response.close();
      return objectMapper.readValue(r, CompletionResult.class);
    } catch (IOException e) {
      log.error(e.getLocalizedMessage());
    }
    return new CompletionResult();
  }
}
