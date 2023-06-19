package com.shoppiem.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.shoppiem.api.data.postgres.entity.ChatHistoryEntity;
import com.shoppiem.api.data.postgres.entity.FcmTokenEntity;
import com.shoppiem.api.data.postgres.repo.ChatHistoryRepo;
import com.shoppiem.api.data.postgres.repo.FcmTokenRepo;
import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.service.chromeExtension.ExtensionServiceImpl;
import com.shoppiem.api.service.chromeExtension.ExtensionServiceImpl.MessageType;
import com.shoppiem.api.service.embedding.EmbeddingService;
import com.shoppiem.api.service.openai.completion.CompletionRequest;
import com.shoppiem.api.service.openai.completion.CompletionResult;
import com.shoppiem.api.service.openai.completion.CompletionMessage;
import com.shoppiem.api.service.utils.JobSemaphore;
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
  private final JobSemaphore jobSemaphore;
  private final FcmTokenRepo fcmTokenRepo;

  @Override
  public CompletionRequest buildGptRequest(String query, String productSku) {
    List<String> embeddings = embeddingService.fetchEmbeddings(query, productSku);
    if (embeddings.size() == 0) {
      return null;
    }
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
  public void callGpt(String query, String registrationToken, String productSku) {
    Thread.startVirtualThread(() -> saveToChatHistory(query, productSku, registrationToken, false));
    jobSemaphore.getChatJobSemaphore().release();
    CompletionRequest request = buildGptRequest(query, productSku);
    if (request == null) {
      sendFcmMessage("Sorry, I'm unable to find answers for this product. Maybe try again with a different product?", registrationToken, productSku);
    } else {
      try {
        String json = objectMapper.writeValueAsString(request);
        CompletionResult result = gptHttpRequest(json);
        if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
            result.getChoices().get(0).getMessage() != null) {
          String response = result.getChoices().get(0).getMessage().getContent();
          Thread.startVirtualThread(
              () -> saveToChatHistory(response, productSku, registrationToken, true));
          sendFcmMessage(response, registrationToken, productSku);
        }
      } catch (JsonProcessingException e) {
        log.error(e.getLocalizedMessage());
      } finally {
        jobSemaphore.getChatJobSemaphore().release();
      }
    }
  }

  private void saveToChatHistory(String query, String productSku, String fcmToken, boolean isGpt) {
    ChatHistoryEntity entity = new ChatHistoryEntity();
    entity.setMessage(query);
    entity.setIsGpt(isGpt);
    entity.setProductSku(productSku);
    entity.setChatId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_CHAT_UID_LENGTH));

    FcmTokenEntity fcmTokenEntity = fcmTokenRepo.findByRegistrationToken(fcmToken);
    if (fcmTokenEntity == null) {
      fcmTokenEntity = new FcmTokenEntity();
      fcmTokenEntity.setRegistrationToken(fcmToken);
      fcmTokenRepo.save(fcmTokenEntity);
    }
    entity.setFcmTokenId(fcmTokenEntity.getId());
    chatHistoryRepo.save(entity);
  }

  private void sendFcmMessage(String chatMessage, String registrationToken, String productSku) {
    Message message = Message.builder()
        .putData("content", chatMessage)
        .putData("productSku", productSku)
        .putData("type", MessageType.CHAT)
        .setToken(registrationToken)
        .build();
    try {
      FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error(e.getLocalizedMessage());
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
