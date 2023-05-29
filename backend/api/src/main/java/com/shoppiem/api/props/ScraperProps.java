package com.shoppiem.api.props;

import com.shoppiem.api.dto.Pair;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 05/29/2023
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "scraper")
public class ScraperProps {
    private String userAgent;
    private List<String> proxyIpAddress;

    public Pair<String, Integer> getProxyIpWithPort() {
        String[] tokens = proxyIpAddress.get(0).split(":");
        return new Pair<>(tokens[0], Integer.parseInt(tokens[1]));
    }
}
