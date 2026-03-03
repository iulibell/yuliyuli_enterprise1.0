package com.yuliyuli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitMqConfig {

    /**
     * 视频收藏配置（包含死信）
     */
    public static final String COLLECT_EXCHANGE_NAME = "collect_exchange";
    public static final String COLLECT_QUEUE_NAME = "collect_queue";
    public static final String COLLECT_ROUTING_KEY = "collect_routing_key";
    public static final String COLLECT_DEAD_EXCHANGE_NAME = "collect_dead_exchange";
    public static final String COLLECT_DEAD_QUEUE_NAME = "collect_dead_queue";
    public static final String COLLECT_DEAD_ROUTING_KEY = "collect_dead_routing_key";

    /**
     * 视频点赞配置（包含死信）
     */
    public static final String LIKE_EXCHANGE_NAME = "like_exchange";
    public static final String LIKE_QUEUE_NAME = "like_queue";
    public static final String LIKE_ROUTING_KEY = "like_routing_key";
    public static final String LIKE_DEAD_EXCHANGE_NAME = "like_dead_exchange";
    public static final String LIKE_DEAD_QUEUE_NAME = "like_dead_queue";
    public static final String LIKE_DEAD_ROUTING_KEY = "like_dead_routing_key";

    /**
     * 视频分发配置（包含死信）
     */
    public static final String VIDEO_EXCHANGE_NAME = "video_exchange";
    public static final String VIDEO_QUEUE_NAME = "video_queue";
    public static final String VIDEO_ROUTING_KEY = "video_routing_key";
    public static final String VIDEO_DEAD_EXCHANGE_NAME = "video_dead_exchange";
    public static final String VIDEO_DEAD_QUEUE_NAME = "video_dead_queue";
    public static final String VIDEO_DEAD_ROUTING_KEY = "video_dead_routing_key";
    
    /**
     * 视频分发
     */
    @Bean
    public Queue videoDeadQueue() {
        return QueueBuilder.durable(VIDEO_DEAD_QUEUE_NAME).build();
    }
    @Bean
    public Exchange videoDeadExchange() {
        return ExchangeBuilder.directExchange(VIDEO_DEAD_EXCHANGE_NAME).durable(true).build();
    }
    @Bean
    public Binding videoDeadBinding() {
        return BindingBuilder.bind(videoDeadQueue())
        .to(videoDeadExchange())
        .with(VIDEO_DEAD_ROUTING_KEY).noargs();
    }
    @Bean
    public Queue videoQueue() {
        return QueueBuilder.durable(VIDEO_QUEUE_NAME)
        .deadLetterExchange(VIDEO_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(VIDEO_DEAD_ROUTING_KEY).build();
    }
    @Bean
    public Exchange videoExchange() {
        return ExchangeBuilder.topicExchange(VIDEO_EXCHANGE_NAME).durable(true).build();
    }
    @Bean
    public Binding videoBinding() {
        return BindingBuilder.bind(videoQueue())
        .to(videoExchange())
        .with(VIDEO_ROUTING_KEY).noargs();
    }

    /**
     * 视频点赞
     */
    @Bean
    public Queue likeDeadQueue() {
        return QueueBuilder.durable(LIKE_DEAD_QUEUE_NAME).build();
    }
    @Bean
    public Exchange likeDeadExchange() {
        return ExchangeBuilder.directExchange(LIKE_DEAD_EXCHANGE_NAME).durable(true).build();
    }
    @Bean
    public Binding likeDeadBinding() {
        return BindingBuilder.bind(likeDeadQueue())
                .to(likeDeadExchange())
                .with(LIKE_DEAD_ROUTING_KEY).noargs();
    }
    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(LIKE_QUEUE_NAME)
                .deadLetterExchange(LIKE_DEAD_EXCHANGE_NAME)
                .deadLetterRoutingKey(LIKE_DEAD_ROUTING_KEY)
                .build();
    }
    @Bean
    public Exchange likeExchange() {
        return ExchangeBuilder.topicExchange(LIKE_EXCHANGE_NAME).durable(true).build();
    }
    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue())
                .to(likeExchange())
                .with(LIKE_ROUTING_KEY).noargs();
    }

    /**
     * 视频收藏
     */
    @Bean
    public Queue collectDeadQueue() {
        return QueueBuilder.durable(COLLECT_DEAD_QUEUE_NAME).build();
    }

    // 收藏死信交换机
    @Bean
    public Exchange collectDeadExchange() {
        return ExchangeBuilder.directExchange(COLLECT_DEAD_EXCHANGE_NAME).durable(true).build();
    }

    // 收藏死信队列绑定
    @Bean
    public Binding collectDeadBinding() {
        return BindingBuilder.bind(collectDeadQueue())
                .to(collectDeadExchange())
                .with(COLLECT_DEAD_ROUTING_KEY).noargs();
    }

    // 收藏业务队列
    @Bean
    public Queue collectQueue() {
        return QueueBuilder.durable(COLLECT_QUEUE_NAME)
                .deadLetterExchange(COLLECT_DEAD_EXCHANGE_NAME)
                .deadLetterRoutingKey(COLLECT_DEAD_ROUTING_KEY).build();
    }

    // 收藏交换机
    @Bean
    public Exchange collectExchange() {
        return ExchangeBuilder.topicExchange(COLLECT_EXCHANGE_NAME).durable(true).build();
    }

    // 收藏队列绑定交换机
    @Bean
    public Binding collectBinding() {
        return BindingBuilder.bind(collectQueue())
                .to(collectExchange())
                .with(COLLECT_ROUTING_KEY).noargs();
    }
}
