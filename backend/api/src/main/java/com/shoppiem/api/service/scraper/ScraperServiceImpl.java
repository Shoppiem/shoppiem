package com.shoppiem.api.service.scraper;

import static com.amazonaws.util.StringUtils.UTF8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.dto.ScrapingJob.JobType;
import com.shoppiem.api.props.InfaticaProps;
import com.shoppiem.api.service.parser.AmazonParser;
import com.shoppiem.api.service.utils.JobSemaphore;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
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
    private final InfaticaProps infaticaProps;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void scrape(String sku, String url, JobType type, boolean scheduleJobs, int numRetries,
        boolean headless) {
        log.info("Scraping {} at {}", sku, url);
        Merchant merchant = getPlatform(url);
        String soup = null;
        try {
            if (headless) {
                soup = downloadPageHeadless(url);
            } else {
                soup = downloadPage(url);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        } finally {
            jobSemaphore.getScrapeJobSemaphore().release();
        }
        // Although we want to retry if parsing any of the pages fails, it is okay to skip failing
        // ones. The most important page is the product page. But the HTML soup for that is
        // provided by the client.
        if (soup != null) {
            final String _soup = soup;
            Thread.startVirtualThread(() -> {
                if (Objects.requireNonNull(merchant) == Merchant.AMAZON) {
                    ProductEntity entity = productRepo.findByProductSku(sku);
//                saveFile(soup,  url);
                    switch (type) {
                        case PRODUCT_PAGE -> amazonParser.parseProductPage(sku, _soup, scheduleJobs,
                            null);
                        case QUESTION_PAGE -> amazonParser.parseProductQuestions(entity, _soup, scheduleJobs);
                        case REVIEW_PAGE -> amazonParser.parseReviewPage(entity, _soup);
                        case ANSWER_PAGE -> amazonParser.parseProductAnswers(sku, _soup);
                    }
                }
            });
        }
    }

    private String downloadPageHeadless(String url) {
        WebClient client = scraperUtils.getWebClient();
        HtmlPage page;
        try {
            page = client.getPage(url);
            int statusCode = page.getWebResponse().getStatusCode();
            if (statusCode >= 400) {
                throw new RuntimeException(
                    String.format("Status Code: %s URL: %s", statusCode, url));
            }
            return page.getWebResponse().getContentAsString();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    private String downloadPage(String url) throws IOException {
        String soup = null;
        if (url != null) {
            List<Header> headers = getHeaders();
            try (CloseableHttpClient httpClient = HttpClients
                .custom()
                .setDefaultHeaders(headers)
                .setUserAgent(userAgentService.getRandomUserAgent())
//                .setProxy(HttpHost.create(scraperProps.getRandomProxy()))
                .build()) {
                HttpPost httpPost = new HttpPost(infaticaProps.getUrl());
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                StringEntity body = getBody(headers, url);
                httpPost.setEntity(body);
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
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

    private StringEntity getBody(List<Header> headers, String url) {
        Map<String, Object> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("api_key", infaticaProps.getKey());
        body.put("url", url);
        body.put("render_js", true);
        body.put("return_json", false);
        body.put("headers", headerMap);
        try {
            return new StringEntity(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
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
            new BasicHeader("sec-fetch-dest", "document"),
            new BasicHeader("sec-fetch-mode", "navigate"),
            new BasicHeader("sec-fetch-site", "same-origin"),
            new BasicHeader("sec-ch-viewport-width", String.valueOf(width)),
            new BasicHeader("viewport-width", String.valueOf(width)),
            new BasicHeader("upgrade-insecure-requests", String.valueOf(1))
        );
    }

    private void saveFile(String html, String path) {
        path = "product/" + path
            .replace("//:", "_")
            .replace("/", "_")
            .replace(":", "")
            .replace(".", "_") + ".html";
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
