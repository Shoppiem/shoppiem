package com.shoppiem.api.service.parser;

/**
 * @author Biz Melesse created on 5/29/23
 */
public interface AmazonParser {
  String bodyDelimiter = "--------SHOPPIEM_BOUNDARY--------";
  void processSoup(String sku, String soup);

}
