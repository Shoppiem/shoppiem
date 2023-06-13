package com.shoppiem.api.service.chat;

import com.shoppiem.api.data.postgres.entity.EmbeddingEntity;
import com.shoppiem.api.service.embedding.EmbeddingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 6/12/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
  private final EmbeddingService embeddingService;

  @Override
  public String buildContext(String query, String productSku) {
    List<String> embeddings = embeddingService.fetchEmbeddings(query, productSku);
    String context = "";
    return context;
  }

  @Override
  public String callGpt(String query, String productSku) {
    String context = buildContext(query, productSku);
    return null;
  }
}
