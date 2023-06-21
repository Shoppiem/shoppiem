package com.shoppiem.api.service.embedding;

import com.shoppiem.api.data.postgres.entity.EmbeddingEntity;
import com.shoppiem.api.data.postgres.entity.ProductAnswerEntity;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.entity.ProductQuestionEntity;
import com.shoppiem.api.data.postgres.entity.ReviewEntity;
import com.shoppiem.api.service.openai.embedding.EmbeddingRequest;
import com.shoppiem.api.service.openai.embedding.EmbeddingResult;
import java.util.List;

/**
 * @author Biz Melesse created on 6/10/23
 */
public interface EmbeddingService {

  Double[] embedUserQuery(String query, String productSku);
  List<String> fetchEmbeddings(String query, String productSku);
  void embedByProductSku(String productSku);
  void embedProduct(ProductEntity productEntity);
  void embedReviews(List<ReviewEntity> reviewsToEmbed, String productSku);
  void embedQuestionsAndAnswers(List<ProductQuestionEntity> questionsToEmbed,
      List<ProductAnswerEntity> answersToEmbed, String productSku);

  EmbeddingResult getEmbeddingResult(EmbeddingRequest embeddingRequest);

}
