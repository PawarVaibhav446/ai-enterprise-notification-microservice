package com.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names from application.yml
    @Value("${rabbitmq.queues.retry}")
    private String retryQueueName;

    @Value("${rabbitmq.queues.dead-letter}")
    private String deadLetterQueueName;

    @Value("${rabbitmq.exchanges.retry}")
    private String retryExchangeName;

    @Value("${rabbitmq.routing-keys.retry}")
    private String retryRoutingKey;

    /**
     * Dead Letter Queue - Where messages go after max retries
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(deadLetterQueueName)
                .build();
    }

    /**
     * Retry Queue - Where failed notifications wait for retry
     */
    @Bean
    public Queue retryQueue() {
        return QueueBuilder
                .durable(retryQueueName)
                .withArgument("x-dead-letter-exchange", "")  // Send to DLQ if rejected
                .withArgument("x-dead-letter-routing-key", deadLetterQueueName)
                .build();
    }

    /**
     * Retry Exchange - Routes messages to retry queue
     */
    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(retryExchangeName);
    }

    /**
     * Binding - Connects exchange to queue using routing key
     */
    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange retryExchange) {
        return BindingBuilder
                .bind(retryQueue)
                .to(retryExchange)
                .with(retryRoutingKey);
    }

    /**
     * Message Converter - Converts objects to JSON
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate - Used to send messages
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * Listener Container Factory - Configures message listeners
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(3);  // Number of concurrent consumers
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}