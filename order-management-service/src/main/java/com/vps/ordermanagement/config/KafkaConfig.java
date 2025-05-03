package com.vps.ordermanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    @Value("${kafka.topics.orders-retry}")
    private String ordersRetryTopic;

    @Value("${kafka.topics.orders-dead}")
    private String ordersDeadTopic;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ordersTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersRetryTopic() {
        return TopicBuilder.name(ordersRetryTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersDeadTopic() {
        return TopicBuilder.name(ordersDeadTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
} 