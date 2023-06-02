package com.shoppiem.api.service.parser;

import java.util.List;

/**
 * @author Biz Melesse created on 5/29/23
 */
public interface AmazonParser {
  String bodyDelimiter = "\n--------SHOPPIEM_BOUNDARY--------\n";
  void processSoup(String sku, String soup);

  List<String> generateReviewLinks(String sku);

  List<String> generateProductQuestionLinks(String sku);

  List<String> generateAnswerLinks(String questionId);

}
