package com.shoppiem.api.service.parser;

import java.util.List;

/**
 * @author Biz Melesse created on 5/29/23
 */
public interface AmazonParser {
  String bodyDelimiter = "\n--------SHOPPIEM_BOUNDARY--------\n";
  void parseProductPage(String sku, String soup);

  void parseReviewPage(Long productId, String soup);

  void parseProductQuestions(Long productId, String soup);

  void parseProductAnswers(String questionId, String soup);

  List<String> generateReviewLinks(String sku);

  List<String> generateProductQuestionLinks(String sku);

  List<String> generateAnswerLinks(String questionId);

}
