package com.shoppiem.api.service.scraper;

import static com.amazonaws.util.StringUtils.UTF8;

import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ScrapingJobDto.JobType;
import com.shoppiem.api.props.ScraperProps;
import com.shoppiem.api.service.parser.AmazonParser;
import com.shoppiem.api.service.utils.JobSemaphore;
import com.shoppiem.api.service.utils.ShoppiemUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.util.EntityUtils;
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
    private final UserAgentService userAgentService;
    private final ScraperProps scraperProps;

    @SneakyThrows
    @Override
    public void scrape(String sku, String url, JobType type, boolean scheduleJobs, int numRetries) {
        String cleanUrl = cleanupUrl(url);
        try {
            log.info("Scraping {} at {}", sku, cleanUrl);
            Merchant merchant = getPlatform(cleanUrl);
//            WebClient client = scraperUtils.getWebClient();
            if (type.equals(JobType.REVIEW_PAGE)) return;
            String soup = downloadPage(cleanUrl);
//            HtmlPage page = client.getPage(cleanUrl);
//            int statusCode = page.getWebResponse().getStatusCode();
//            if (statusCode >= 200 && statusCode < 400 ) {
//                String soup = page.getWebResponse().getContentAsString();
                if (Objects.requireNonNull(merchant) == Merchant.AMAZON) {
                    ProductEntity entity = productRepo.findByProductSku(sku);
                    saveFile(soup,  "product/" + cleanUrl
                        .replace("//:", "_")
                        .replace("/", "_")
                        .replace(":", "")
                        .replace(".", "_") + ".html");
                    switch (type) {
                        case PRODUCT_PAGE -> amazonParser.parseProductPage(sku, soup, scheduleJobs);
                        case QUESTION_PAGE -> amazonParser.parseProductQuestions(entity, soup, scheduleJobs);
                        case REVIEW_PAGE -> amazonParser.parseReviewPage(entity, soup);
                        case ANSWER_PAGE -> amazonParser.parseProductAnswers(sku, soup);

                    }

//                }
            }
        } catch (Exception e) {
            log.error("{}: {}", e.getLocalizedMessage(), cleanUrl);
            if (numRetries > 0) {
                log.info("Retrying {}", cleanUrl);
                scrape(sku, url, type, scheduleJobs, numRetries - 1);
                Thread.sleep(2000L);
            } else {
                log.info("Retries exhausted for {}", cleanUrl);
            }
        } finally {
            jobSemaphore.getSemaphore().release();
        }
    }


//    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
//        "accept-encoding": "gzip, deflate, br",
//        "accept-language": "en",
//        "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36",


    private String downloadPage(String url) throws IOException {
        String soup = null;
        if (url != null) {
            try (CloseableHttpClient httpClient = HttpClients
                .custom()
                .setDefaultHeaders(getHeaders())
//                .setUserAgent(userAgentService.getRandomUserAgent())
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

    private List<Header> getHeaders() {
        int min = 600;
        int max = 920;
        int width = new Random().nextInt((max - min) + 1) + min;
        return List.of(
            new BasicHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"),
            new BasicHeader(HttpHeaders.CONTENT_LANGUAGE, "en"),
            new BasicHeader(HttpHeaders.CONTENT_LANGUAGE, "en"),
            new BasicHeader(HttpHeaders.USER_AGENT, userAgentService.getRandomUserAgent()),
            new BasicHeader("sec-ch-ua", "\"Chromium\";v=\"112\", \"Google Chrome\";v=\"112\", \"Not:A-Brand\";v=\"99\""),
            new BasicHeader("sec-fetch-dest", "document"),
            new BasicHeader("sec-fetch-mode", "navigate"),
            new BasicHeader("sec-fetch-site", "same-origin"),
            new BasicHeader("sec-ch-viewport-width", String.valueOf(width)),
            new BasicHeader("viewport-width", String.valueOf(width)),
            new BasicHeader("upgrade-insecure-requests", String.valueOf(1))
        );

//        device-memory: 8
//        downlink: 10
//        dpr: 2
//        ect: 4g
//        rtt: 50
//        sec-ch-device-memory: 8
//        sec-ch-dpr: 2
//
//        sec-ch-ua-mobile: ?0
//        sec-ch-ua-platform: "macOS"
//        sec-ch-viewport-width: 940
//
//        sec-fetch-user: ?1
//        upgrade-insecure-requests: 1
//        user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36
//        viewport-width: 940
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
            return String.format("%s://%s%s", parsedUrl.getProtocol(), parsedUrl.getHost(), parsedUrl.getPath());
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
