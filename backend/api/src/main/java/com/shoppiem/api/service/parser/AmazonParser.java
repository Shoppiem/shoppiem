package com.shoppiem.api.service.parser;

import com.shoppiem.api.data.postgres.entity.ProductEntity;
import java.util.List;

/**
 * @author Biz Melesse created on 5/29/23
 */
public interface AmazonParser {
  String bodyDelimiter = "\n--------SHOPPIEM_BOUNDARY--------\n";
  void parseProductPage(String sku, String soup, boolean scheduleJobs);

  void createScrapingJobs(ProductEntity entity);

  void parseReviewPage(ProductEntity entity, String soup, boolean scheduleJobs);

  void parseProductQuestions(ProductEntity entity, String soup, boolean scheduleJobs);

  void parseProductAnswers(String questionId, String soup);

  List<String> generateReviewLinks(ProductEntity entity);

  List<String> generateProductQuestionLinks(ProductEntity entity);

  List<String> generateAnswerLinks(String questionId);

}
