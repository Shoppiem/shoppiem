package com.shoppiem.api.service.parser;

import com.shoppiem.api.data.postgres.entity.ProductEntity;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Biz Melesse created on 5/29/23
 */
public interface AmazonParser {

  /**
   * Us an in-memory cache to keep track of reviews and questions
   * that have been scraped already. This helps us avoid making expensive
   * database calls.
   */
  Map<String, Boolean> existingReviewIds = new ConcurrentHashMap<>();
  Map<String, Boolean> existingQuestionIds = new ConcurrentHashMap<>();

  String bodyDelimiter = "\n--------SHOPPIEM_BOUNDARY--------\n";
  void parseProductPage(String sku, String soup, boolean scheduleJobs);

  void scheduleQandAScraping(ProductEntity entity);

  void scheduleInitialReviewScraping(ProductEntity entity);

  void parseReviewPage(ProductEntity entity, String soup);

  void parseProductQuestions(ProductEntity entity, String soup, boolean scheduleJobs);

  void parseProductAnswers(String questionId, String soup);

  List<String> generateReviewLinks(ProductEntity entity);

  List<String> generateProductQuestionLinks(ProductEntity entity);

  List<String> generateAnswerLinks(String questionId);

}
