package com.shoppiem.api.service.parser;

import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 5/29/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonParserImpl implements AmazonParser {

  @Override
  public void processSoup(String sku, String soup) {
    Document doc = Jsoup.parse(soup);
    String titleXPath = "//*[@id=\"productTitle\"]";
    String sellerXPath = "//*[@id=\"bylineInfo\"]";
    String ratingXPath = "//*[@id=\"acrPopover\"]/span[1]/a";
    String reviewCountXPath = "//*[@id=\"acrCustomerReviewText\"]";
    String featuresXPath = "//*[@id=\"feature-bullets\"]/ul/li[1]/span";
    String overviewFeatureXPath = "//*[@id=\"productOverview_feature_div\"]/div/table/tbody";
    String productDescriptionXPath = "//*[@id=\"productDescription\"]/p[1]/span";
    String imageXPath = "//*[@id=\"landingImage\"]";
    String imageUrl = getImage(doc, imageXPath);
    Double rating = getRating(doc, ratingXPath);
    String title = getTitle(doc, titleXPath);
    String seller = getSeller(doc, sellerXPath);
    Integer numReviews = getNumReviews(doc, reviewCountXPath);

    List<String> features = new ArrayList<>();
    walkHelper(doc, featuresXPath, features);


    List<String> overviewTableData = new ArrayList<>();
    walkHelper(doc, overviewFeatureXPath, overviewTableData);
    overviewTableData = mapTableColumns(overviewTableData);

    List<String> productDescription = new ArrayList<>();
    walkHelper(doc, productDescriptionXPath, productDescription);

    

    String bookDescFeatureXPath = "//*[@id=\"bookDescription_feature_div\"]/div/div[1]/p[1]/span[4]";




    String productDescriptionXPathType2 = "//*[@id=\"aplus\"]/div/div[1]/div/p[1]";


  }

  private String getImage(Document doc, String imageXPath) {
    for (Attribute attribute : doc.selectXpath(imageXPath).get(0).attributes()) {
      if (attribute.getKey().equals("src")) {
        return attribute.getValue();
      }
    }
     return "";
  }

  private Integer getNumReviews(Document doc, String reviewCountXPath) {
    return Integer.parseInt(doc.selectXpath(reviewCountXPath)
        .get(0)
        .childNodes().get(0)
        .toString().replace(" ratings", ""));
  }

  private String getSeller(Document doc, String sellerXPath) {
    return StringUtils.capitalize(
        doc.selectXpath(sellerXPath).get(0).childNodes().get(0).toString().replace("Visit ", ""));
  }

  private String getTitle(Document doc, String titleXPath) {
    return doc.selectXpath(titleXPath).get(0).childNodes().get(0).toString();
  }

  private Double getRating(Document doc, String ratingXPath) {
    String ratingClass = "a-size-base a-color-base";
    double rating = 0.0;
    for (Element element : doc.selectXpath(ratingXPath)) {
      for (Node childNode : element.childNodes()) {
        if (childNode.attr("class").equals(ratingClass)) {
          rating = Double.parseDouble(childNode.childNodes().get(0).toString());
        }
      }
    }
    return rating;
  }

  private void walkHelper(Document doc, String xPath, List<String> ret) {
    for (Element element : doc.selectXpath(xPath)) {
      walk(element, ret);
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

  private void walk(Node root, List<String> allText) {
    if (root != null) {
      if (root instanceof TextNode) {
        String text = ((TextNode) root)
            .text()
            .replace("See more", "")
            .trim()
            .strip();
        if (!ObjectUtils.isEmpty(text)) {
          allText.add(text);
        }
      } else if (root instanceof  Element) {
        for (Node childNode : ((Element) root).childNodes()) {
          walk(childNode, allText);
        }
      }
    }
  }

}
