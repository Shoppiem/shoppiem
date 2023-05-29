package com.shoppiem.api.service.scraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.shoppiem.api.service.parser.AmazonParser;
import java.net.MalformedURLException;
import java.net.URL;
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

    @Override
    public boolean getContent(String sku, String url) {
        try { // TODO: check that the content can be processed before scraping
            Merchant merchant = getPlatform(url);
            WebClient client = scraperUtils.getWebClient();
            String cleanUrl = cleanupUrl(url);
            HtmlPage page = client.getPage(cleanUrl);
            int statusCode = page.getWebResponse().getStatusCode();
            if (statusCode >= 200 && statusCode < 400 ) {
                String soup = page.getWebResponse().getContentAsString();
                switch (merchant) {
                    case AMAZON:
                        amazonParser.processSoup(sku, soup);
                        break;
                    default:
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return false;
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
