package com.shoppiem.api.service.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
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

  private final Consumer consumer;

//  @Value("${rabbitmq.queue.name}")
//  private String queue;

//  @Value("${rabbitmq.queue.json.name}")
  private String jobsQueue = "jobs";

//  @Value("${rabbitmq.exchange.name}")
  private String exchange = "shoppiem-exchange";

//  @Value("${rabbitmq.routing.key}")
  private String routingKey = "shoppiem.job.#";

////  @Value("${rabbitmq.routing.json.key}")
//  private String routingJsonKey = "shoppiem.job.#";

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
//    connectionFactory.setAddresses("http://localhost");
    connectionFactory.setUsername("user");
    connectionFactory.setPassword("password");
    return connectionFactory;
  }

  @Bean
  public Queue jobQue(){
    return new Queue(jobsQueue);
  }

  // spring bean for rabbitmq exchange
  @Bean
  public TopicExchange exchange(){
    return new TopicExchange(exchange);
  }

  // binding between queue and exchange using routing key
  @Bean
  public Binding binding(){
    return BindingBuilder
        .bind(jobQue())
        .to(exchange())
        .with(routingKey);
  }

//  // binding between json queue and exchange using routing key
//  @Bean
//  public Binding jsonBinding(){
//    return BindingBuilder
//        .bind(jsonQueue())
//        .to(exchange())
//        .with(routingJsonKey);
//  }

  @Bean
  SimpleMessageListenerContainer container(MessageListenerAdapter listenerAdapter,
      ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setConcurrentConsumers(100);
    container.setQueueNames(jobsQueue);
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter() {
    return new MessageListenerAdapter(consumer, "consume");
  }
//
//  @Bean
//  public MessageConverter converter(){
//    return new Jackson2JsonMessageConverter();
//  }

//  @Bean
//  public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
//    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//    rabbitTemplate.setMessageConverter(converter());
//    return rabbitTemplate;
//  }

}
