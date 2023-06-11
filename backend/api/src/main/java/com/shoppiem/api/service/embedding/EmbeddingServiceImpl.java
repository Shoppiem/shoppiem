package com.shoppiem.api.service.embedding;

import com.shoppiem.api.data.postgres.entity.ProductAnswerEntity;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.entity.ProductQuestionEntity;
import com.shoppiem.api.data.postgres.entity.ReviewEntity;
import com.shoppiem.api.data.postgres.repo.ProductAnswerRepo;
import com.shoppiem.api.data.postgres.repo.ProductQuestionRepo;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.data.postgres.repo.ReviewRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    Thread.startVirtualThread(() -> embedReviews(reviewsToEmbed));
    Thread.startVirtualThread(() -> embedQuestionsAndAnswers(questionsToEmbed, answersToEmbed));
    Thread.startVirtualThread(() -> embedReviews(reviewsToEmbed));
  }

  @Override
  public void embedProduct(ProductEntity productEntity) {

  }

  @Override
  public void embedReviews(List<ReviewEntity> reviewsToEmbed) {

  }

  @Override
  public void embedQuestionsAndAnswers(List<ProductQuestionEntity> questionsToEmbed,
      List<ProductAnswerEntity> answersToEmbed) {

  }
}
