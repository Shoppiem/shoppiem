package com.shoppiem.api.service;

import com.shoppiem.api.props.RabbitMQProps;
import com.shoppiem.api.service.messaging.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * @author Biz Melesse created on 6/4/23
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfiguration {

  private final RabbitMQProps rabbitMQProps;

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitMQProps.getHost());
    connectionFactory.setUsername(rabbitMQProps.getUsername());
    connectionFactory.setPassword(rabbitMQProps.getPassword());
    return connectionFactory;
  }

  @Bean
  public Queue jobQue(){
    return new Queue(rabbitMQProps.getJobQueue());
  }

  @Bean
  public TopicExchange exchange(){
    return new TopicExchange(rabbitMQProps.getTopicExchange());
  }


  @Bean
  public Binding binding(){
    return BindingBuilder
        .bind(jobQue())
        .to(exchange())
        .with(rabbitMQProps.getRoutingKey());
  }

  @Bean
  SimpleMessageListenerContainer container(MessageListenerAdapter listenerAdapter,
      ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setConcurrentConsumers(rabbitMQProps.getConsumerConcurrency());
    container.setQueueNames(rabbitMQProps.getJobQueue());
    container.setMessageListener(listenerAdapter);
    return container;
  }

}
