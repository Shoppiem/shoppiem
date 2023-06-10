package com.shoppiem.api.service.scraper;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersion.BrowserVersionBuilder;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.shoppiem.api.dto.Pair;
import com.shoppiem.api.props.ScraperProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Bizuwork Melesse
 * created on 6/22/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperUtils {
    private final ErrorListener errorListener;
    private final ScraperProps scraperProps;

    public WebClient getWebClient() {
        // User user agent
        BrowserVersionBuilder browserVersion = new BrowserVersion.BrowserVersionBuilder(BrowserVersion.CHROME)
            .setUserAgent(scraperProps.getUserAgent());
        browserVersion.setUserAgent(scraperProps.getUserAgent());
        WebClient webClient = new WebClient(browserVersion.build());
        webClient.addRequestHeader("User-Agent", scraperProps.getUserAgent());

        // Set proxy
        Pair<String, Integer> proxyIpWithPort = scraperProps.getProxyIpWithPort();
        ProxyConfig proxyConfig = new ProxyConfig(proxyIpWithPort.getFirst(), proxyIpWithPort.getSecond(), "http");
        webClient.getOptions().setProxyConfig(proxyConfig);

        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setTimeout(60000);
        webClient.setJavaScriptErrorListener(errorListener.javaScriptErrorListener());
        return webClient;
    }
}
