package com.shoppiem.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.shoppiem.api.data.postgres.entity.ChatHistoryEntity;
import com.shoppiem.api.data.postgres.entity.FcmTokenEntity;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ChatHistoryRepo;
import com.shoppiem.api.data.postgres.repo.EmbeddingRepo;
import com.shoppiem.api.data.postgres.repo.FcmTokenRepo;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ChatJob;
import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.props.RabbitMQProps;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMQProps rabbitMQProps;
  private final ProductRepo productRepo;
  private final EmbeddingRepo embeddingRepo;

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
  public void callGpt(String query, String registrationToken, String productSku, boolean inRetry) {
    if (isReady(productSku)) {
      Thread.startVirtualThread(
          () -> saveToChatHistory(query, productSku, registrationToken, false));
      CompletionRequest request = buildGptRequest(query, productSku);
      if (request == null) {
        Message message = Message.builder()
            .putData("content", "Sorry, I'm unable to find answers for this product. Maybe try again with a different product?")
            .putData("productSku", productSku)
            .putData("type", MessageType.CHAT)
            .setToken(registrationToken)
            .build();
        sendFcmMessage(message);
      } else {
        try {
          String json = objectMapper.writeValueAsString(request);
          CompletionResult result = gptHttpRequest(json);
          if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
              result.getChoices().get(0).getMessage() != null) {
            String response = result.getChoices().get(0).getMessage().getContent();
            Thread.startVirtualThread(
                () -> saveToChatHistory(response, productSku, registrationToken, true));
            Message message = Message.builder()
                .putData("content", response)
                .putData("productSku", productSku)
                .putData("type", MessageType.CHAT)
                .setToken(registrationToken)
                .build();
            sendFcmMessage(message);
          }
        } catch (JsonProcessingException e) {
          log.error(e.getLocalizedMessage());
        } finally {
          jobSemaphore.getChatJobSemaphore().release();
        }
      }
    } else {
      jobSemaphore.getChatJobSemaphore().release();
      addQueryToQueue(query, registrationToken, productSku, 5_000, true);
      if (!inRetry) {
        Message message = Message.builder()
            .putData("content",
                "We are currently processing this product. Please try again in 1-2 minutes.")
            .putData("productSku", productSku)
            .putData("type", MessageType.CHAT)
            .setToken(registrationToken)
            .build();
        sendFcmMessage(message);
      }
    }
  }

  private boolean isReady(String productSku) {
    ProductEntity entity = productRepo.findByProductSku(productSku);
    if (entity != null) {
      if (entity.getIsReady()) {
        return true;
      }
      // If 50% or more of the total documents for this product have been embedded, consider it to be
      // ready to start querying.
      int productPageEmbeddings = 3; // on average we treat the product page as 3 documents
      long numDocuments = productPageEmbeddings + entity.getNumReviews() + entity.getNumQuestionsAnswered();
      long numDocumentsEmbedded = embeddingRepo.countDocumentsEmbedded(productSku);
      if (numDocumentsEmbedded/(1.0 * numDocuments) >= 0.5) {
        entity.setIsReady(true);
        productRepo.save(entity);
        return true;
      }
    }
    return false;
  }

  @Override
  public void addQueryToQueue(String query, String fcmToken, String productSku, long delay, boolean inRetry) {
    Thread.startVirtualThread(() -> {
      ChatJob job = new ChatJob();
      job.setId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_UID_LENGTH));
      job.setQuery(query);
      job.setFcmToken(fcmToken);
      job.setProductSku(productSku);
      job.setInRetry(inRetry);
      // If the product is ready, add the job to the queue immediately. Otherwise, delay the queueing
      try {
        String jobString = objectMapper.writeValueAsString(job);
        Thread.sleep(delay);
        rabbitTemplate.convertAndSend(
            rabbitMQProps.getTopicExchange(),
            rabbitMQProps.getChatJobRoutingKeyPrefix() + productSku,
            jobString);
      } catch (JsonProcessingException | InterruptedException e) {
        e.printStackTrace();
      }
    });
  }

  private void saveToChatHistory(String query, String productSku, String fcmToken, boolean isGpt) {
    ChatHistoryEntity entity = new ChatHistoryEntity();
    entity.setMessage(query);
    entity.setIsGpt(isGpt);
    entity.setProductSku(productSku);
    entity.setChatId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_CHAT_UID_LENGTH));

    FcmTokenEntity fcmTokenEntity = fcmTokenRepo.findByFcmToken(fcmToken);
    if (fcmTokenEntity == null) {
      fcmTokenEntity = new FcmTokenEntity();
      fcmTokenEntity.setFcmToken(fcmToken);
      fcmTokenRepo.save(fcmTokenEntity);
    }
    entity.setFcmTokenId(fcmTokenEntity.getId());
    chatHistoryRepo.save(entity);
  }

  private void sendFcmMessage(Message message) {
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
