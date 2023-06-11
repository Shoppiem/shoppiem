package com.shoppiem.api.props;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(value = {
    FirebaseProps.class,
    FlywayProps.class,
    PostgresDataSourceProps.class,
    SourceProps.class,
    DefaultUserProps.class,
    OpenAiProps.class,
    UserProps.class,
    IpAddressProps.class,
    ScraperProps.class,
    RabbitMQProps.class,
    InfaticaProps.class
})
public class PropConfiguration {
}
