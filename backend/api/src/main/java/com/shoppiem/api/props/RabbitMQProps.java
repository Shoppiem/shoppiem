package com.shoppiem.api.props;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 06/05/2023
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProps {
    private String jobQueue;
    private String topicExchange;
    private String routingKey;
    private String routingKeyPrefix;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer consumerConcurrency;
}
