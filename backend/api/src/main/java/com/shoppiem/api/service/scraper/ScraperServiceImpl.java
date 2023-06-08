package com.shoppiem.api.service.scraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ScrapingJobDto.JobType;
import com.shoppiem.api.service.parser.AmazonParser;
import com.shoppiem.api.service.utils.JobSemaphore;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Bizuwork Melesse
 * created on 6/17/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperServiceImpl implements ScraperService {
    private final AmazonParser amazonParser;
    private final ScraperUtils scraperUtils;
    private final JobSemaphore jobSemaphore;
    private final ProductRepo productRepo;

    @Override
    public void scrape(String sku, String url, JobType type, boolean scheduleJobs) {
        try { // TODO: check that the content can be processed before scraping
            log.info("Scraping {} at {}", sku, url);
            Merchant merchant = getPlatform(url);
            WebClient client = scraperUtils.getWebClient();
            String cleanUrl = cleanupUrl(url);
            HtmlPage page = client.getPage(cleanUrl);
            int statusCode = page.getWebResponse().getStatusCode();
            if (statusCode >= 200 && statusCode < 400 ) {
                String soup = page.getWebResponse().getContentAsString();
                if (Objects.requireNonNull(merchant) == Merchant.AMAZON) {
                    ProductEntity entity = productRepo.findByProductSku(sku);
                    switch (type) {
                        case PRODUCT_PAGE -> amazonParser.parseProductPage(sku, soup, scheduleJobs);
                        case QUESTION_PAGE -> amazonParser.parseProductQuestions(entity, soup, scheduleJobs);
                        case REVIEW_PAGE -> amazonParser.parseReviewPage(entity, soup);
                        case ANSWER_PAGE -> amazonParser.parseProductAnswers(sku, soup);

                    }

                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        } finally {
            jobSemaphore.getSemaphore().release();
        }
    }

    private String cleanupUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            return String.format("%s://%s/%s", parsedUrl.getProtocol(), parsedUrl.getHost(), parsedUrl.getPath());
        } catch (MalformedURLException e) {
            return url;
        }
    }

    private Merchant getPlatform(String url) {
        URL parsedUrl = null;
        try {
            parsedUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (parsedUrl != null) {
            if (parsedUrl.getHost().toLowerCase().contains("amazon")) {
                return Merchant.AMAZON;
            }
        }
        return Merchant.AMAZON;
    }
}
