package com.shoppiem.api.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.shoppiem.api.data.postgres.entity.ProductAnswerEntity;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.entity.ProductQuestionEntity;
import com.shoppiem.api.data.postgres.entity.ReviewEntity;
import com.shoppiem.api.data.postgres.repo.ProductAnswerRepo;
import com.shoppiem.api.data.postgres.repo.ProductQuestionRepo;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.data.postgres.repo.ReviewRepo;
import com.shoppiem.api.dto.ScrapingJob;
import com.shoppiem.api.dto.ScrapingJob.JobType;
import com.shoppiem.api.props.RabbitMQProps;
import com.shoppiem.api.service.chromeExtension.ExtensionServiceImpl.MessageType;
import com.shoppiem.api.service.embedding.EmbeddingService;
import com.shoppiem.api.service.scraper.Merchant;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 5/29/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonParserImpl implements AmazonParser {
  private final ProductRepo productRepo;
  private final ProductQuestionRepo questionRepo;
  private final ReviewRepo reviewRepo;
  private final ProductAnswerRepo answerRepo;
  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMQProps rabbitMQProps;
  private final EmbeddingService embeddingService;

  @PostConstruct
  public void populateIdCache() {
    for (ReviewEntity reviewEntity : reviewRepo.findAll()) {
      existingReviewIds.put(reviewEntity.getReviewId(), true);
    }
    for (ProductQuestionEntity productQuestionEntity : questionRepo.findAll()) {
      existingQuestionIds.put(productQuestionEntity.getQuestionId(), true);
    }
  }

  @Override
  public void parseProductPage(String sku, String soup, boolean scheduleJobs, String fcmToken) {
    Document doc = Jsoup.parse(soup);
    String titleXPath = "//*[@id=\"productTitle\"]";
    String sellerXPath = "//*[@id=\"bylineInfo\"]";
    String starRatingXPath = "//*[@id=\"acrPopover\"]/span[1]/a";
    String reviewCountXPath = "//*[@id=\"acrCustomerReviewText\"]";
    String featuresXPath = "//*[@id=\"feature-bullets\"]";
    String overviewFeatureXPath = "//*[@id=\"productOverview_feature_div\"]/div/table/tbody";
    String productDescriptionXPath = "//*[@id=\"productDescription\"]/p[1]/span";
    String productDescriptionXPathType2 = "//*[@id=\"aplus\"]";
    String bookDescFeatureXPath = "//*[@id=\"bookDescription_feature_div\"]";
    String imageXPath = "//*[@id=\"landingImage\"]";
    String canonicalXPath = "//link[@rel=\"canonical\"]";
    String numQuestionsAnsweredXPath = "//*[@id=\"askATFLink\"]/span";
    String imageUrl = getImage(doc, imageXPath);
    Double starRating = getStarRating(doc, starRatingXPath);
    String title = getTitle(doc, titleXPath);
    String seller = getSeller(doc, sellerXPath);
    Double price = getPrice(doc);
    Long numReviews = getNumReviews(doc, reviewCountXPath);
    Long numQuestionsAnswered = getNumQuestionsAnswered(doc, numQuestionsAnsweredXPath);
    String canonicalUrl = getCanonicalUrl(doc, canonicalXPath);
    Thread.startVirtualThread(() -> sendProductInfoToClient(title, imageUrl, fcmToken, sku));
    List<String> features = new ArrayList<>();
    walkHelper(doc, featuresXPath, features, 3, true);

    List<String> overviewTableData = new ArrayList<>();
    walkHelper(doc, overviewFeatureXPath, overviewTableData, 3, true);
    overviewTableData = mapTableColumns(overviewTableData);

    List<String> productDescription = new ArrayList<>();
    walkHelper(doc, productDescriptionXPath, productDescription, 3, true);

    List<String> productDescriptionType2 = new ArrayList<>();
    walkHelper(doc, productDescriptionXPathType2, productDescriptionType2, 3, true);

    List<String> bookDescription = new ArrayList<>();
    walkHelper(doc, bookDescFeatureXPath, bookDescription, 3, true);

    ProductEntity entity = productRepo.findByProductSku(sku);
    entity.setNumReviews(numReviews);
    entity.setStarRating(starRating);
    entity.setNumQuestionsAnswered(numQuestionsAnswered);
    entity.setCurrency("USD");
    entity.setUpdatedAt(LocalDateTime.now());
    entity.setImageUrl(imageUrl);
    entity.setPrice(price);
    entity.setProductUrl(canonicalUrl);
    entity.setSeller(seller);
    entity.setTitle(ShoppiemUtils.truncate(title));
    entity.setDescription(combineDescriptionData(List.of(
        features,
        overviewTableData,
        productDescription,
        productDescriptionType2,
        bookDescription)));
    productRepo.save(entity);
    Thread.startVirtualThread(() -> embeddingService.embedProduct(entity));
    if (scheduleJobs) {
      scheduleQandAScraping(entity);
      scheduleInitialReviewScraping(entity);
    }
  }

  private void sendProductInfoToClient(String title, String imageUrl, String fcmToken, String productSku) {
    Message message = Message.builder()
        .putData("name", title)
        .putData("imageUrl", imageUrl)
        .putData("productSku", productSku)
        .putData("type", MessageType.PRODUCT_INFO_REQUEST)
        .setToken(fcmToken)
        .build();
    try {
      FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error(e.getLocalizedMessage());
    }
  }

  @Override
  public void scheduleQandAScraping(ProductEntity entity) {
    List<ScrapingJob> jobs = new ArrayList<>();
    List<String> questionUrls = generateProductQuestionLinks(entity);
    Collections.shuffle(questionUrls);
    for (String url : questionUrls) {
      ScrapingJob job = new ScrapingJob();
      job.setProductSku(entity.getProductSku());
      job.setId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_UID_LENGTH));
      job.setUrl(url);
      job.setType(JobType.QUESTION_PAGE);
      jobs.add(job);
    }
    submitJobs(jobs);
  }

  @Override
  public void scheduleInitialReviewScraping(ProductEntity entity) {
    // Set the number of reviews to cover only the first page of the reviews if numReviews is greater
    // than 0. The real review count is on that page, which we will have to scrape first and parse
    // downstream.
    List<ScrapingJob> jobs = new ArrayList<>();
    List<String> reviewUrls = generateReviewLinks(entity);
    if (reviewUrls.size() > 0) {
      String url = reviewUrls.get(0);
      ScrapingJob job = new ScrapingJob();
      job.setProductSku(entity.getProductSku());
      job.setId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_UID_LENGTH));
      job.setUrl(url);
      job.setType(JobType.REVIEW_PAGE);
      jobs.add(job);
      submitJobs(jobs);
    } else {
      log.info("Product {} has no reviews. Skipping review scraping...",
          entity.getProductSku());
    }
  }


  private void submitJobs(List<ScrapingJob> jobs) {
    for (ScrapingJob job : jobs) {
      try {
        String jobString = objectMapper.writeValueAsString(job);
        rabbitTemplate.convertAndSend(
            rabbitMQProps.getTopicExchange(),
            rabbitMQProps.getScrapeJobRoutingKeyPrefix() + job.getProductSku(),
            jobString);
      } catch (JsonProcessingException e) {
        log.error(e.getLocalizedMessage());
      }
    }
  }

  @Override
  public void parseReviewPage(ProductEntity productEntity, String soup) {
    Document doc = Jsoup.parse(soup);
    String allReviewsXPath = "//*[@id=\"cm_cr-review_list\"]";
    Map<String, ReviewEntity> reviews = new HashMap<>();
    ReviewEntity entity = new ReviewEntity();
    entity.setProductId(productEntity.getId());
    if (!productEntity.getAllReviewsScheduled()) {
      scheduleAllReviewScraping(soup, productEntity.getProductSku());
    }
    for (Element element : doc.selectXpath(allReviewsXPath)) {
      walkReviewsHelper(element, reviews);
    }
    List<ReviewEntity> allReviews = reviews.values()
        .stream()
        .peek(it -> {
          it.setProductId(productEntity.getId());
          it.setMerchant(Merchant.AMAZON.name());
        })
        .filter(it -> !existingReviewIds.containsKey(it.getReviewId()))
        .collect(Collectors.toList());
    for (ReviewEntity review : allReviews) {
      existingReviewIds.put(review.getReviewId(), true);
    }
    reviewRepo.saveAll(allReviews);
    Thread.startVirtualThread(() ->
        embeddingService.embedReviews(allReviews, productEntity.getProductSku()));
  }

  private void scheduleAllReviewScraping(String soup, String productSku) {
    long numReviews = 0;
    ProductEntity entity = productRepo.findByProductSku(productSku);
    try {
       numReviews = Long.parseLong(soup.substring(
              soup.indexOf("total ratings"), soup.indexOf("with reviews"))
          .split("total ratings")[1]
          .replace(",","").strip());
    } catch (Exception e) {
      e.printStackTrace();
      long duration = getRandomSleepTime();
      log.info("Sleeping for {} ms and re-trying initial review scraping for {}",
          duration, productSku);
      try {
        Thread.sleep(duration);
      } catch (InterruptedException ex) {
        log.error(ex.getLocalizedMessage());
      }
      scheduleInitialReviewScraping(entity);
    }
    if (numReviews > 0) {
      entity.setNumReviews(numReviews);
      entity.setAllReviewsScheduled(true);
      productRepo.save(entity);
      log.info("Total reviews: {}", numReviews);
      List<String> reviewUrls = generateReviewLinks(entity);
      reviewUrls.remove(0); // We already scraped the first page so no need to do it again
      Collections.shuffle(reviewUrls);
      List<ScrapingJob> jobs = new ArrayList<>();
      for (String url : reviewUrls) {
        ScrapingJob job = new ScrapingJob();
        job.setProductSku(entity.getProductSku());
        job.setId(ShoppiemUtils.generateUid(ShoppiemUtils.DEFAULT_UID_LENGTH));
        job.setUrl(url);
        job.setType(JobType.REVIEW_PAGE);
        jobs.add(job);
      }
      submitJobs(jobs);
    }
  }

  private long getRandomSleepTime() {
    int min = 10;
    int max = 1000;
    return new Random().nextLong((max - min) + 1) + min;
  }

  @Override
  public void parseProductQuestions(ProductEntity productEntity, String soup, boolean scheduleJobs) {
    Document doc = Jsoup.parse(soup);
    Map<String, QuestionAnswerContainer> questions = new HashMap<>();
    for (Node childNode : doc.childNodes()) {
      questionWalk(childNode, false, questions);
    }
    List<QuestionAnswerContainer> newQuestions = new ArrayList<>();
    for (QuestionAnswerContainer value : questions.values()) {
      String questionId = value.getQuestion().getQuestionId();
      if (!existingQuestionIds.containsKey(questionId)) {
        existingQuestionIds.put(questionId, true);
        newQuestions.add(value);
      }
    }

    Long productId = productEntity.getId();
    List<ProductQuestionEntity> questionEntities = newQuestions
        .stream()
        .map(it -> {
          ProductQuestionEntity entity = it.getQuestion();
          entity.setProductId(productId);
          entity.setNumAnswers(entity.getNumAnswers() != null ? entity.getNumAnswers() : 1);
          entity.setUpvotes(entity.getUpvotes() != null ? entity.getUpvotes() : 0);
          return entity;
        })
        .collect(Collectors.toList());
    questionRepo.saveAll(questionEntities);


    // Map the saved question entity IDs to the original question IDs from the DOM
    Map<String, Long> questionIds = new HashMap<>();
    for (ProductQuestionEntity questionEntity : questionEntities) {
      questionIds.put(questionEntity.getQuestionId(), questionEntity.getId());
    }

    List<ProductAnswerEntity> answerEntities = newQuestions
        .stream()
        .map(it -> {
          ProductAnswerEntity answerEntity = it.getAnswer();
          answerEntity.setProductId(productId);
          answerEntity.setProductQuestionId(questionIds.get(it.getQuestion().getQuestionId()));
          answerEntity.setUpvotes(answerEntity.getUpvotes() != null ? answerEntity.getUpvotes() : 0);
          return answerEntity;
    }).collect(Collectors.toList());
    answerRepo.saveAll(answerEntities);
    Thread.startVirtualThread(() ->
        embeddingService.embedQuestionsAndAnswers(questionEntities, answerEntities,
        productEntity.getProductSku()));
  }

  private void questionWalk(Node root, boolean isQuestionDiv,
      Map<String, QuestionAnswerContainer> questions) {
    String questionListDivClass = "a-section askTeaserQuestions";
    String questionCardClass = "a-fixed-left-grid a-spacing-base";
    if (root instanceof Element) {
      for (Attribute attribute : root.attributes()) {
        if (attribute.getKey().equals("class") &&
            attribute.getValue().equals(questionListDivClass)) {
          // This is the main div of list of questions and answers
          isQuestionDiv = true;
          break;
        } else if (attribute.getKey().equals("class") &&
            attribute.getValue().equals(questionCardClass) && isQuestionDiv) {
          // This is an individual question/answer card
          questionCardWalk(root, questions, new QuestionId());
        }
      }
      for (Node childNode : root.childNodes()) {
        questionWalk(childNode, isQuestionDiv, questions);
      }
    }
  }

  private void questionCardWalk(Node root, Map<String, QuestionAnswerContainer> questions,
      QuestionId questionId) {
    if (root != null) {
      if (root instanceof Element) {
        for (Attribute attribute : root.attributes()) {
          String key = attribute.getKey();
          String value = attribute.getValue();
          ProductQuestionEntity questionEntity = null;
          ProductAnswerEntity answerEntity = null;
          String _questionId = questionId.getQuestionId();
          if (!ObjectUtils.isEmpty(_questionId)) {
            var container =  questions.get(_questionId);
            questionEntity = container.getQuestion();
            answerEntity = container.getAnswer();
          }
          if (key.equals("action") && value.contains("/ask/vote")) {
            _questionId = value.split("/vote/question/")[1].strip();
            questionId.setQuestionId(_questionId);
            var container = questions
                .getOrDefault(_questionId, new QuestionAnswerContainer());
            questionEntity = container.getQuestion();
            questionEntity.setQuestionId(_questionId);
            questions.put(_questionId, container);
          } else if (!ObjectUtils.isEmpty(_questionId)) {
            if (key.equals("data-count")) {
              long upvotes = Long.parseLong(value);
              questionEntity.setUpvotes(upvotes);
            } else if (key.equals("id") && value.startsWith("question-")) {
              List<String> values = new ArrayList<>();
              walk(root, values, 1, false);
              questionEntity.setQuestion(values.get(1));
            } else if (key.equals("id") && value.startsWith("askSeeAllAnswersLink")) {
              List<String> values = new ArrayList<>();
              walk(root, values, 1, false);
              questionEntity.setNumAnswers(getNumAnswers(values));
            } else if (key.equals("class") && value.startsWith("a-profile-name")) {
              List<String> values = new ArrayList<>();
              walk(root, values, 1, false);
              if (values.size() > 0) {
                answerEntity.setAnsweredBy(values.get(0));
              }
            }  else if (key.equals("class") && value.startsWith("askLongText")) {
              List<String> values = new ArrayList<>();
              walk(root, values, 1, false);
              answerEntity.setAnswer(getLongAnswer(values));
            }
            else if (key.equals("class") && value.startsWith("a-fixed-left-grid-col a-col-right") &&
                answerEntity.getAnswer() == null) {
              List<String> values = new ArrayList<>();
              walk(root, values, 1, false);
              answerEntity.setAnswer(getShortAnswer(values));
            } else if (key.equals("class") && value.startsWith("a-color-tertiary aok-align-center")) {
              List<String> values = new ArrayList<>();
              walk(root, values, 1, false);
              answerEntity.setAnsweredAt(getAnsweredAt(values));
            }
          }
        }
        for (Node childNode : root.childNodes()) {
          questionCardWalk(childNode, questions, questionId);
        }
      }
    }
  }

  private String getShortAnswer(List<String> values) {
    for (int i = 0; i < values.size() - 1; i++) {
      if (values.get(i).startsWith("Answer:")) {
        return values.get(i + 1);
      }
    }
    return "";
  }

  private LocalDateTime getAnsweredAt(List<String> values) {
    for (String value : values) {
      LocalDateTime time = parseDate(value);
      if (time != null) {
        return time;
      }
    }
    return null;
  }

  private LocalDateTime parseDate(String value) {
    String date = value.replaceAll("[^A-Za-z0-9\s]", "").strip();
    DateFormat fmt = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
    try {
      Date d = fmt.parse(date);
      return LocalDateTime.ofInstant(d.toInstant(),
          ZoneOffset.UTC);
    } catch (ParseException e) {}
    return null;
  }

  private String getLongAnswer(List<String> values) {
    return values.stream()
        .map(it -> it
            .replace("See more", "")
            .replace("see more", "")
            .replace("See less", "")
            .replace("see less", "")
            .strip())
        .filter(it -> !ObjectUtils.isEmpty(it))
        .collect(Collectors.joining(" "));
  }

  private Long getNumAnswers(List<String> values) {
    for (String value : values) {
      if (value.toLowerCase().contains("see all")) {
        Long.parseLong(value.replaceAll("[^0-9]", ""));
      }
    }
    return 0L;
  }

  @Override
  public void parseProductAnswers(String questionId, String soup) {
    Document doc = Jsoup.parse(soup);
    Map<String, ProductAnswerEntity> answers = new HashMap<>();
    for (Node childNode : doc.childNodes()) {
      walkAnswer(childNode, answers);
    }
    ProductQuestionEntity questionEntity = questionRepo.findByQuestionId(questionId);
    Long productId = questionEntity.getProductId();
    Long productQuestionId = questionEntity.getId();
    List<ProductAnswerEntity> entities = answers.values()
        .stream()
        .peek(it -> {
          it.setProductQuestionId(productQuestionId);
          it.setProductId(productId);
        }).collect(Collectors.toList());
    answerRepo.saveAll(entities);
  }

  private void walkAnswer(Node root, Map<String, ProductAnswerEntity> answers) {
    if (root != null) {
      for (Attribute attribute : root.attributes()) {
        String key = attribute.getKey();
        String value = attribute.getValue();
        if (key.equals("id") && value.startsWith("answer-")) {
          String answerId = value.split("answer-")[1];
          List<String> values = new ArrayList<>();
          walk(root, values, 1, false);
          // Find the date index in the list of values parsed and use that as reference to parse the other
          // values in the list
          int dateIndex = findDateIndex(values);
          if (dateIndex > 0) {
            ProductAnswerEntity answerEntity = new ProductAnswerEntity();
            LocalDateTime date = parseDate(values.get(dateIndex));
            String answeredBy = values.get(dateIndex-1);
            String answer = String.join(" ", values.subList(0, dateIndex-1))
                .replace("\n", "");
            answerEntity.setAnswerId(answerId);
            answerEntity.setAnsweredAt(date);
            answerEntity.setAnsweredBy(answeredBy);
            answerEntity.setAnswer(answer);
            if (values.get(dateIndex + 1).contains("found this helpful")) {
              String votes = values.get(dateIndex + 1).split("found this helpful")[0];
              if (votes.contains("of")) {
                long upvotes = Long.parseLong(votes.split("of")[0].strip());
                long totalVotes = Long.parseLong(votes.split("of")[1].strip());
                long downvotes = totalVotes - upvotes;
                answerEntity.setUpvotes(upvotes);
                answerEntity.setDownvotes(downvotes);
              }
            }
            answers.put(answerId, answerEntity);
          } else {
            log.error("Unable to parse answer. Date index not found");
          }
        }
      }
      for (Node childNode : root.childNodes()) {
        walkAnswer(childNode, answers);
      }
    }
  }

  private int findDateIndex(List<String> values) {
    for (int i = 0; i < values.size(); i++) {
      LocalDateTime time = parseDate(values.get(i));
      if (time != null) {
        return i;
      }
    }
    return -1;
  }



  private void walkReviewsHelper(Node root, Map<String, ReviewEntity> reviews) {
    if (root != null) {
      for (Attribute attribute : root.attributes()) {
        if (attribute.getValue().contains("-review-card")) {
          String reviewId = attribute.getValue().split("-review-card")[0];
          ReviewEntity entity = new ReviewEntity();
          entity.setReviewId(reviewId);
          reviews.put(reviewId, entity);
          walkReviewCard(root, reviewId, reviews);
        }
      }
      if (root instanceof  Element) {
        for (Node childNode : root.childNodes()) {
          walkReviewsHelper(childNode, reviews);
        }
      }
    }
  }

  private void walkReviewCard(Node root, String reviewId, Map<String, ReviewEntity> reviews) {
    if (root != null) {
       if (root instanceof Element) {
        for (Node childNode : root.childNodes()) {
          for (Attribute attribute : childNode.attributes()) {
            if (attribute.getKey().equals("data-hook")) {
              String value = attribute.getValue();
              ReviewEntity reviewEntity = reviews.get(reviewId);
              if (value.equals("review-title")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, true);
                if (values.size() > 0) {
                  reviewEntity.setTitle(values.get(0));
                }
              } else if (value.equals("review-body")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, false);
                reviewEntity.setBody(String.join(" ", values));
              } else if (value.equals("review-date")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, true);
                if (values.size() > 0) {
                  String[] tokens = values.get(0).split(" on ");
                  String country = tokens[0].split("Reviewed in ")[1];
                  String date = tokens[1];
                  DateFormat fmt = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
                  try {
                    Date d = fmt.parse(date);
                    reviewEntity.setSubmittedAt(LocalDateTime.ofInstant(d.toInstant(),
                        ZoneOffset.UTC));
                  } catch (ParseException e) {
                    e.printStackTrace();
                  }
                  reviewEntity.setCountry(country);
                }
              } else if (value.equals("review-voting-widget")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, true);
                reviewEntity.setUpvotes(getReviewUpvotes(values));
              } else if (value.equals("genome-widget")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, true);
                if (values.size() > 0) {
                  reviewEntity.setReviewer(values.get(0));
                }
              } else if (value.equals("review-star-rating")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, false);
                if (values.size() > 0) {
                  reviewEntity.setStarRating(
                      Math.round(Float.parseFloat(
                          values.get(0).replace(" out of 5 stars", ""))));
                }
              } else if (value.equals("avp-badge")) {
                List<String> values = new ArrayList<>();
                walk(childNode, values, 1, false);
                if (values.size() > 0 && values.get(0).toLowerCase().contains("verified purchase")) {
                  reviewEntity.setVerifiedPurchase(true);
                }
              }
            }
          }
          walkReviewCard(childNode, reviewId, reviews);
        }
      }
    }

  }

  private Long getReviewUpvotes(List<String> values) {
    for (String s : values) {
      if (s.contains("people found this helpful")) {
        return Long.parseLong(s.replace(" people found this helpful", ""));
      }
    }
    return 0L;
  }

  private Long getNumQuestionsAnswered(Document doc, String numQuestionsAnsweredXPath) {
    List<String> values = new ArrayList<>();
    walkHelper(doc, numQuestionsAnsweredXPath, values, 1, false);
    if (values.size() > 0) {
      return Long.parseLong(values.get(0).replace(" answered questions", ""));
    }
    return 0L;
  }

  @Override
  public List<String> generateReviewLinks(ProductEntity entity) {
    List<String> urls = new ArrayList<>();
    if (entity != null) {
      String sku = entity.getProductSku();
      long numReviews = entity.getNumReviews();
      long numPages = numReviews / 10;
      if (numReviews % 10 > 0) {
        numPages++;
      }
      for (int i = 0; i < numPages; i++) {
        String prefix = entity.getProductUrl().split("/dp/")[0];
        String url = "";
        int page = i + 1;
        if (i == 0) {
          url = String.format("%s/product-reviews/%s/ref=cm_cr_getr_d_paging_btm_prev_%s?ie=UTF8&reviewerType=all_reviews&pageNumber=%s",
              prefix, sku, page, page);
        } else {
          url = String.format("%s/product-reviews/%s/ref=cm_cr_getr_d_paging_btm_next_%s?ie=UTF8&reviewerType=all_reviews&pageNumber=%s",
              prefix, sku, page, page);
        }
        urls.add(url);
      }
    }
    return urls;
  }

  @Override
  public List<String> generateProductQuestionLinks(ProductEntity entity) {
    List<String> urls = new ArrayList<>();
    if (entity != null) {
      String sku = entity.getProductSku();
      long numQuestions = entity.getNumQuestionsAnswered();
      long numPages = numQuestions / 10;
      if (numQuestions % 10 > 0) {
        numPages++;
      }
      for (int i = 0; i < numPages; i++) {
        int page = i + 1;
        String url = String.format("https://www.amazon.com/ask/questions/asin/%s/%s/ref=ask_ql_psf_ql_hza?isAnswered=true",
            sku, page);
        urls.add(url);
      }
    }
    return urls;
  }

  @Override
  public List<String> generateAnswerLinks(String questionId) {
    ProductQuestionEntity entity = questionRepo.findByQuestionId(questionId);
    List<String> urls = new ArrayList<>();
    if (entity != null) {
      long numAnswers = entity.getNumAnswers();
      long numPages = numAnswers / 10;
      if (numAnswers % 10 > 0) {
        numPages++;
      }
      for (int i = 0; i < numPages; i++) {
        int page = i + 1;
        String url = String.format("https://www.amazon.com/ask/questions/%s/%s/ref=ask_al_psf_al_hza",
            questionId, page);
        urls.add(url);
      }
    }
    return urls;
  }

  private String combineDescriptionData(List<List<String>> data) {
    List<String> toCombine = new ArrayList<>();
    for (List<String> item : data) {
      if (item.size() > 0) {
        toCombine.add(String.join(" ", item));
      }
    }
    return String.join(bodyDelimiter, toCombine);
  }

  private String getCanonicalUrl(Document doc, String canonicalXPath) {
    for (Element element : doc.selectXpath(canonicalXPath)) {
      for (Attribute attribute : element.attributes()) {
        if (attribute.getKey().equals("href")) {
          return attribute.getValue();
        }
      }
    }
    return "";
  }


  private Double getPrice(Document doc) {
    // TODO: Currently defaulting the currency to USD. The symbol for the actual currency is
    // in the list returned so use that in the future.
    List<String> priceValues = new ArrayList<>();
    for (Node childNode : doc.childNodes()) {
      if (priceValues.size() == 0) {
        priceWalk(childNode, priceValues);
      }
    }
    for (String value : priceValues) {
      try {
        return Double.parseDouble(value.replace("$", ""));
      } catch (Exception e) {
        //pass
      }
    }
    return 0.0;
  }

  private void priceWalk(Node root, List<String> priceValues) {
    if (root != null && priceValues.size() == 0) {
      if (root instanceof Element) {
        for (Attribute attribute : root.attributes()) {
          if (attribute.getKey().equals("class") && attribute.getValue().contains("a-price")) {
            walk(root, priceValues, 1, false);
            return;
          }
        }
        for (Node childNode : root.childNodes()) {
          priceWalk(childNode, priceValues);
        }
      }
    }
  }

  private String getImage(Document doc, String imageXPath) {
    for (Element element : doc.selectXpath(imageXPath)) {
      for (Attribute attribute : element.attributes()) {
        if (attribute.getKey().equals("src")) {
          return attribute.getValue();
        }
      }
    }
     return "";
  }

  private Long getNumReviews(Document doc, String reviewCountXPath) {
    return Long.parseLong(doc.selectXpath(reviewCountXPath)
        .get(0)
        .childNodes().get(0)
        .toString()
        .replace(" ratings", "")
        .replace(",", ""));
  }

  private String getSeller(Document doc, String sellerXPath) {
    return StringUtils.capitalize(
        doc.selectXpath(sellerXPath).get(0).childNodes().get(0).toString().replace("Visit ", ""));
  }

  private String getTitle(Document doc, String titleXPath) {
    return doc.selectXpath(titleXPath).get(0).childNodes().get(0).toString();
  }

  private Double getStarRating(Document doc, String starRatingXPath) {
    String ratingClass = "a-size-base a-color-base";
    double rating = 0.0;
    for (Element element : doc.selectXpath(starRatingXPath)) {
      for (Node childNode : element.childNodes()) {
        if (childNode.attr("class").equals(ratingClass)) {
          rating = Double.parseDouble(childNode.childNodes().get(0).toString());
        }
      }
    }
    return rating;
  }

  private void walkHelper(Document doc, String xPath, List<String> ret, int minTokenLength, boolean alphanumeric) {
    for (Element element : doc.selectXpath(xPath)) {
      walk(element, ret, minTokenLength, alphanumeric);
    }
  }

  private List<String> mapTableColumns(List<String> tableData) {
    // Remap table columns into key-value pairs
    // Ideally, there should be an equal number of keys and values
    String key = "";
    List<String> map = new ArrayList<>();
    for (int i = 0; i < tableData.size(); i++) {
      if (i % 2 == 0) {
        key = tableData.get(i);
      } else {
        map.add(key + ": " + tableData.get(i));
        key = "";
      }
    }
    return map;
  }

  private void walk(Node root, List<String> allText, int minTokenLength, boolean alphanumeric) {
    if (root != null) {
      if (root instanceof TextNode) {
        String text = ((TextNode) root)
            .text()
            .replace("See more", "")
            .trim()
            .strip();
        if (alphanumeric) {
          text = text.replaceAll("[^A-Za-z0-9. ]", ""); // Remove all non-alphanumeric characters
        }
        if (!ObjectUtils.isEmpty(text)) {
          // Only add strings that are longer than two words
          if (text.split(" ").length >= minTokenLength) {
            allText.add(text);
          }
        }
      } else if (root instanceof  Element) {
        for (Node childNode : root.childNodes()) {
          walk(childNode, allText, minTokenLength, alphanumeric);
        }
      }
    }
  }

  @Getter @Setter
  private static class QuestionAnswerContainer {
    private ProductQuestionEntity question = new ProductQuestionEntity();
    private ProductAnswerEntity answer = new ProductAnswerEntity();
  }

  @Getter @Setter
  private static class QuestionId {
    private String questionId = null;
  }

}
