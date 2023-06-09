package com.shoppiem.api.service.scraper;

import static com.amazonaws.util.StringUtils.UTF8;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ScrapingJobDto.JobType;
import com.shoppiem.api.dto.UserAgents;
import com.shoppiem.api.props.ScraperProps;
import com.shoppiem.api.service.parser.AmazonParser;
import com.shoppiem.api.service.utils.JobSemaphore;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    private final UserAgentService userAgentService;
    private final ScraperProps scraperProps;

    @Override
    public void scrape(String sku, String url, JobType type, boolean scheduleJobs) {
        try { // TODO: check that the content can be processed before scraping
            log.info("Scraping {} at {}", sku, url);
            Merchant merchant = getPlatform(url);
//            WebClient client = scraperUtils.getWebClient();
            String cleanUrl = cleanupUrl(url);
            String soup = downloadPage(cleanUrl);
//            HtmlPage page = client.getPage(cleanUrl);
//            int statusCode = page.getWebResponse().getStatusCode();
//            if (statusCode >= 200 && statusCode < 400 ) {
//                String soup = page.getWebResponse().getContentAsString();
                if (Objects.requireNonNull(merchant) == Merchant.AMAZON) {
                    ProductEntity entity = productRepo.findByProductSku(sku);
                    saveFile(soup,  "product/" + ShoppiemUtils.generateUid() + ".html");
                    switch (type) {
                        case PRODUCT_PAGE -> amazonParser.parseProductPage(sku, soup, scheduleJobs);
                        case QUESTION_PAGE -> amazonParser.parseProductQuestions(entity, soup, scheduleJobs);
                        case REVIEW_PAGE -> amazonParser.parseReviewPage(entity, soup);
                        case ANSWER_PAGE -> amazonParser.parseProductAnswers(sku, soup);

                    }

//                }
            }
        } catch (Exception e) {
            log.error("{}: {}", e.getLocalizedMessage(), url);
        } finally {
            jobSemaphore.getSemaphore().release();
        }
    }

    private String downloadPage(String url) throws IOException {
        String soup = null;
        if (url != null) {
            try (CloseableHttpClient httpClient = HttpClients
                .custom()
                .setUserAgent(userAgentService.getRandomUserAgent())
                .setProxy(HttpHost.create(scraperProps.getRandomProxy()))
                .build()) {
                HttpGet httpGet = new HttpGet(url);
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                soup = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    throw new RuntimeException(
                        String.format("Status Code: %s URL: %s", statusCode, url));
                }
                httpResponse.close();
            }
        }
        return soup;
    }

    private void saveFile(String html, String path) {
        try (InputStream in = new ByteArrayInputStream(html.getBytes(UTF8))) {
            // Save the file to disk
            if (!new File(path).exists()) {
                new File(path).mkdirs();
            }
            File output = new File(path);
            Files.copy(in, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ".saveFile: {}",
                e.getLocalizedMessage());
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
