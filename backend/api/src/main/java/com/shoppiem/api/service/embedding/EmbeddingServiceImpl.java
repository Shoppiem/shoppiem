package com.shoppiem.api.service.embedding;

import com.shoppiem.api.data.postgres.entity.EmbeddingEntity;
import com.shoppiem.api.data.postgres.entity.ProductAnswerEntity;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.entity.ProductQuestionEntity;
import com.shoppiem.api.data.postgres.entity.ReviewEntity;
import com.shoppiem.api.data.postgres.repo.EmbeddingRepo;
import com.shoppiem.api.data.postgres.repo.ProductAnswerRepo;
import com.shoppiem.api.data.postgres.repo.ProductQuestionRepo;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.data.postgres.repo.ReviewRepo;
import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.service.openai.OpenAiService;
import com.shoppiem.api.service.openai.embedding.Embedding;
import com.shoppiem.api.service.openai.embedding.EmbeddingRequest;
import com.shoppiem.api.service.openai.embedding.EmbeddingResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.descriptor.java.DoubleJavaType;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 6/10/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

  private final ProductRepo productRepo;
  private final ReviewRepo reviewRepo;
  private final ProductQuestionRepo questionRepo;
  private final ProductAnswerRepo answerRepo;
  private final OpenAiProps openAiProps;
  private final EmbeddingRepo embeddingRepo;
  private OpenAiService openAiService;
  private final int batchSize = 10;

  @PostConstruct
  public void onStart() {
    openAiService = new OpenAiService(openAiProps.getApiKey(),
        Duration.ofSeconds(openAiProps.getRequestTimeoutSeconds()));
  }

  @Override
  public void embedByProductSku(String productSku) {
    ProductEntity productEntity = productRepo.findByProductSku(productSku);
    if (productEntity == null) {
      log.warn("Product '{}' does not exist. Skipping embedding...", productSku);
      return;
    }
    if (!productEntity.getHasEmbedding()) {
      embedProduct(productEntity);
    }
    // Embed this product's reviews and Q & As
    Long productId = productEntity.getId();
    List<ReviewEntity> reviewsToEmbed = reviewRepo.findAllReviewsToEmbed(productId);
    List<ProductQuestionEntity> questionsToEmbed = questionRepo.findAllQuestionsToEmbed(productId);
    List<ProductAnswerEntity> answersToEmbed = answerRepo.findAllAnswersToEmbed(productId);
    try {
      Thread.sleep(2500L);
      Thread.startVirtualThread(() -> embedReviews(reviewsToEmbed, productSku));
      Thread.sleep(2500L);
      Thread.startVirtualThread(() -> embedQuestionsAndAnswers(questionsToEmbed, answersToEmbed, productSku));
    } catch (InterruptedException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  @Override
  public void embedProduct(ProductEntity productEntity) {

  }

  @Override
  public void embedReviews(List<ReviewEntity> reviewsToEmbed, String productSku) {
    // Treat each review as a document. We don't need to truncate the reviews.
    // OpenAI's token limit for a single document is 8192. That is
    // about 6000 words, so it's safe to assume that no single review will have that many words.

    // Limit the batch size to 100
    ProductEntity productEntity = productRepo.findByProductSku(productSku);
    Long productId = productEntity.getId();
    List<List<?>> batches = getBatches(reviewsToEmbed);
    for (List<?> batch : batches) {
      EmbeddingRequest embeddingRequest = createEmbeddingRequest(productSku);
      List<String> input = new ArrayList<>();
      List<Long> reviewIds = new ArrayList<>();
      for (Object o : batch) {
        ReviewEntity review = (ReviewEntity) o;
        reviewIds.add(review.getId());
        String text = clean(review.getTitle() + " " + review.getBody());
        log.info("Text: {}", text);
        input.add(text);
      }
      embeddingRequest.setInput(input);
      EmbeddingResult result = openAiService.createEmbeddings(embeddingRequest);

      // Save all the embeddings, along with the actual text and update the corresponding
      // reviews
      // Re-fetch the entities with new sessions
      Map<Long, ReviewEntity> reviewEntityMap = mapById(reviewRepo.findAllReviewsByIds(reviewIds));
      List<EmbeddingEntity> embeddingEntities = new ArrayList<>();
      List<Embedding> data = result.getData();
      for (int i = 0; i < result.getData().size(); i++) {
        List<Double> vector = data.get(i).getEmbedding();
        Long reviewId = reviewIds.get(i);
        String text = input.get(i);
        EmbeddingEntity embedding = new EmbeddingEntity();
        embedding.setEmbedding(vector.toArray(new Double[0]));
        embedding.setReviewId(reviewId);
        embedding.setProductId(productId);
        embedding.setText(text);
        embeddingEntities.add(embedding);
        reviewEntityMap.get(reviewId).setHasEmbedding(true);
      }
      embeddingRepo.deleteAll(embeddingRepo.findAllReviewEmbeddingsByIds(reviewIds));
      embeddingRepo.saveAll(embeddingEntities);
      reviewRepo.saveAll(reviewEntityMap.values());
    }
  }

  private Map<Long, ReviewEntity> mapById(List<ReviewEntity> reviewEntities) {
    Map<Long, ReviewEntity> map = new HashMap<>();
    for (ReviewEntity reviewEntity : reviewEntities) {
      map.put(reviewEntity.getId(), reviewEntity);
    }
    return map;
  }

  private EmbeddingRequest createEmbeddingRequest(String productSku) {
    EmbeddingRequest request = new EmbeddingRequest();
    request.setUser(productSku);
    request.setModel(openAiProps.getEmbeddingModel());
    return request;
  }

  private String clean(String s) {
    return s.replaceAll("[^A-Za-z0-9 ]", ""); // Remove all non-alphanumeric characters
  }

  @Override
  public void embedQuestionsAndAnswers(List<ProductQuestionEntity> questionsToEmbed,
      List<ProductAnswerEntity> answersToEmbed, String productSku) {

  }

  private List<List<?>> getBatches(List<?> entities) {
    List<List<?>> batches = new ArrayList<>();
    List<Object> batch = new ArrayList<>();
    for (Object entity : entities) {
      if (batch.size() >= batchSize) {
        batches.add(batch);
        batch.clear();
      }
      batch.add(entity);
    }
    if (batch.size() > 0) {
      batches.add(batch);
    }
    return batches;
  }
}
