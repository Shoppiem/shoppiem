package com.shoppiem.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.service.embedding.EmbeddingService;
import com.shoppiem.api.service.openai.OpenAiService;
import com.shoppiem.api.service.openai.completion.CompletionChoice;
import com.shoppiem.api.service.openai.completion.CompletionRequest;
import com.shoppiem.api.service.openai.completion.CompletionResult;
import com.shoppiem.api.service.openai.completion.Message;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.hc.core5.http.ContentType;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
            new Message("system", openAiProps.getSystemMessage()),
            new Message("user", content)
        )).build();
  }

  @Override
  public void callGpt(String query, String productSku) {
    CompletionRequest request = buildGptRequest(query, productSku);
    try {
      String json = objectMapper.writeValueAsString(request);
      CompletionResult result = gptHttpRequest(json);
      if (result != null && !ObjectUtils.isEmpty(result.getChoices()) &&
          result.getChoices().get(0).getMessage() != null) {
        String response = result.getChoices().get(0).getMessage().getContent();
        log.info("Assistant: {}", response);
      }
    } catch (JsonProcessingException e) {
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
