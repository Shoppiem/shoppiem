package com.shoppiem.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 6/5/23
 */
@Getter @Setter
public class ScrapingJobDto {
  private String url;
  private String productSku;
  private String questionId;
  private JobType type;
  private String id;


  public enum JobType {
    PRODUCT_PAGE,
    QUESTION_PAGE,
    ANSWER_PAGE,
    REVIEW_PAGE
  }
}
