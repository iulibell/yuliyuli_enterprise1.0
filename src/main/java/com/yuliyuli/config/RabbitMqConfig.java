package com.yuliyuli.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  /** 视频删除配置（包含死信） */
  public static final String DELETE_EXCHANGE_NAME = "delete_exchange";

  public static final String DELETE_QUEUE_NAME = "delete_queue";
  public static final String DELETE_ROUTING_KEY = "delete_routing_key";
  public static final String DELETE_DEAD_EXCHANGE_NAME = "delete_dead_exchange";
  public static final String DELETE_DEAD_QUEUE_NAME = "delete_dead_queue";
  public static final String DELETE_DEAD_ROUTING_KEY = "delete_dead_routing_key";

  /** 视频播放配置（包含死信） */
  public static final String PLAY_EXCHANGE_NAME = "play_exchange";

  public static final String PLAY_QUEUE_NAME = "play_queue";
  public static final String PLAY_ROUTING_KEY = "play_routing_key";
  public static final String PLAY_DEAD_EXCHANGE_NAME = "play_dead_exchange";
  public static final String PLAY_DEAD_QUEUE_NAME = "play_dead_queue";
  public static final String PLAY_DEAD_ROUTING_KEY = "play_dead_routing_key";

  /** 热门视频播放配置（包含死信） */
  public static final String HOT_PLAY_EXCHANGE_NAME = "hot_play_exchange";

  public static final String HOT_PLAY_QUEUE_NAME = "hot_play_queue";
  public static final String HOT_PLAY_ROUTING_KEY = "hot_play_routing_key";
  public static final String HOT_PLAY_DEAD_EXCHANGE_NAME = "hot_play_dead_exchange";
  public static final String HOT_PLAY_DEAD_QUEUE_NAME = "hot_play_dead_queue";
  public static final String HOT_PLAY_DEAD_ROUTING_KEY = "hot_play_dead_routing_key";

  /** 视频评论配置（包含死信） */
  public static final String COMMENT_EXCHANGE_NAME = "comment_exchange";

  public static final String COMMENT_QUEUE_NAME = "comment_queue";
  public static final String COMMENT_ROUTING_KEY = "comment_routing_key";
  public static final String COMMENT_DEAD_EXCHANGE_NAME = "comment_dead_exchange";
  public static final String COMMENT_DEAD_QUEUE_NAME = "comment_dead_queue";
  public static final String COMMENT_DEAD_ROUTING_KEY = "comment_dead_routing_key";

  /** 视频收藏配置（包含死信） */
  public static final String COLLECT_EXCHANGE_NAME = "collect_exchange";

  public static final String COLLECT_QUEUE_NAME = "collect_queue";
  public static final String COLLECT_ROUTING_KEY = "collect_routing_key";
  public static final String COLLECT_DEAD_EXCHANGE_NAME = "collect_dead_exchange";
  public static final String COLLECT_DEAD_QUEUE_NAME = "collect_dead_queue";
  public static final String COLLECT_DEAD_ROUTING_KEY = "collect_dead_routing_key";

  /** 视频点赞配置（包含死信） */
  public static final String LIKE_EXCHANGE_NAME = "like_exchange";

  public static final String LIKE_QUEUE_NAME = "like_queue";
  public static final String LIKE_ROUTING_KEY = "like_routing_key";
  public static final String LIKE_DEAD_EXCHANGE_NAME = "like_dead_exchange";
  public static final String LIKE_DEAD_QUEUE_NAME = "like_dead_queue";
  public static final String LIKE_DEAD_ROUTING_KEY = "like_dead_routing_key";

  /** 视频分发配置（包含死信） */
  public static final String VIDEO_EXCHANGE_NAME = "video_exchange";

  public static final String VIDEO_QUEUE_NAME = "video_queue";
  public static final String VIDEO_ROUTING_KEY = "video_routing_key";
  public static final String VIDEO_DEAD_EXCHANGE_NAME = "video_dead_exchange";
  public static final String VIDEO_DEAD_QUEUE_NAME = "video_dead_queue";
  public static final String VIDEO_DEAD_ROUTING_KEY = "video_dead_routing_key";

  /** 视频分发 */
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
        .with(VIDEO_DEAD_ROUTING_KEY)
        .noargs();
  }

  @Bean
  public Queue videoQueue() {
    return QueueBuilder.durable(VIDEO_QUEUE_NAME)
        .deadLetterExchange(VIDEO_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(VIDEO_DEAD_ROUTING_KEY)
        .build();
  }

  @Bean
  public Exchange videoExchange() {
    return ExchangeBuilder.topicExchange(VIDEO_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding videoBinding() {
    return BindingBuilder.bind(videoQueue()).to(videoExchange()).with(VIDEO_ROUTING_KEY).noargs();
  }

  /** 视频点赞 */
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
        .with(LIKE_DEAD_ROUTING_KEY)
        .noargs();
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
    return BindingBuilder.bind(likeQueue()).to(likeExchange()).with(LIKE_ROUTING_KEY).noargs();
  }

  /** 视频收藏 */
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
        .with(COLLECT_DEAD_ROUTING_KEY)
        .noargs();
  }

  // 收藏业务队列
  @Bean
  public Queue collectQueue() {
    return QueueBuilder.durable(COLLECT_QUEUE_NAME)
        .deadLetterExchange(COLLECT_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(COLLECT_DEAD_ROUTING_KEY)
        .build();
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
        .with(COLLECT_ROUTING_KEY)
        .noargs();
  }

  /** 视频评论配置（包含死信） */
  @Bean
  public Queue commentDeadQueue() {
    return QueueBuilder.durable(COMMENT_DEAD_QUEUE_NAME).build();
  }

  @Bean
  public Exchange commentDeadExchange() {
    return ExchangeBuilder.directExchange(COMMENT_DEAD_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding commentDeadBinding() {
    return BindingBuilder.bind(commentDeadQueue())
        .to(commentDeadExchange())
        .with(COMMENT_DEAD_ROUTING_KEY)
        .noargs();
  }

  @Bean
  public Queue commentQueue() {
    return QueueBuilder.durable(COMMENT_QUEUE_NAME)
        .deadLetterExchange(COMMENT_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(COMMENT_DEAD_ROUTING_KEY)
        .build();
  }

  @Bean
  public Exchange commentExchange() {
    return ExchangeBuilder.topicExchange(COMMENT_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding commentBinding() {
    return BindingBuilder.bind(commentQueue())
        .to(commentExchange())
        .with(COMMENT_ROUTING_KEY)
        .noargs();
  }

  /** 热门视频播放配置（包含死信） */
  @Bean
  public Queue hotPlayDeadQueue() {
    return QueueBuilder.durable(HOT_PLAY_DEAD_QUEUE_NAME).build();
  }

  @Bean
  public Exchange hotPlayDeadExchange() {
    return ExchangeBuilder.directExchange(HOT_PLAY_DEAD_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding hotPlayDeadBinding() {
    return BindingBuilder.bind(hotPlayDeadQueue())
        .to(hotPlayDeadExchange())
        .with(HOT_PLAY_DEAD_ROUTING_KEY)
        .noargs();
  }

  @Bean
  public Queue hotPlayQueue() {
    return QueueBuilder.durable(HOT_PLAY_QUEUE_NAME)
        .deadLetterExchange(HOT_PLAY_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(HOT_PLAY_DEAD_ROUTING_KEY)
        .build();
  }

  @Bean
  public Exchange hotPlayExchange() {
    return ExchangeBuilder.topicExchange(HOT_PLAY_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding hotPlayBinding() {
    return BindingBuilder.bind(hotPlayQueue())
        .to(hotPlayExchange())
        .with(HOT_PLAY_ROUTING_KEY)
        .noargs();
  }

  /** 视频播放配置（包含死信） */
  @Bean
  public Queue playQueue() {
    return QueueBuilder.durable(PLAY_QUEUE_NAME)
        .deadLetterExchange(PLAY_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(PLAY_DEAD_ROUTING_KEY)
        .build();
  }

  @Bean
  public Exchange playExchange() {
    return ExchangeBuilder.topicExchange(PLAY_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding playBinding() {
    return BindingBuilder.bind(playQueue()).to(playExchange()).with(PLAY_ROUTING_KEY).noargs();
  }

  @Bean
  public Queue playDeadQueue() {
    return QueueBuilder.durable(PLAY_DEAD_QUEUE_NAME).build();
  }

  @Bean
  public Exchange playDeadExchange() {
    return ExchangeBuilder.directExchange(PLAY_DEAD_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding playDeadBinding() {
    return BindingBuilder.bind(playDeadQueue())
        .to(playDeadExchange())
        .with(PLAY_DEAD_ROUTING_KEY)
        .noargs();
  }

  /** 视频删除配置（包含死信） */
  @Bean
  public Queue deleteQueue() {
    return QueueBuilder.durable(DELETE_QUEUE_NAME)
        .deadLetterExchange(DELETE_DEAD_EXCHANGE_NAME)
        .deadLetterRoutingKey(DELETE_DEAD_ROUTING_KEY)
        .build();
  }

  @Bean
  public Exchange deleteExchange() {
    return ExchangeBuilder.topicExchange(DELETE_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding deleteBinding() {
    return BindingBuilder.bind(deleteQueue())
        .to(deleteExchange())
        .with(DELETE_ROUTING_KEY)
        .noargs();
  }

  @Bean
  public Queue deleteDeadQueue() {
    return QueueBuilder.durable(DELETE_DEAD_QUEUE_NAME).build();
  }

  @Bean
  public Exchange deleteDeadExchange() {
    return ExchangeBuilder.directExchange(DELETE_DEAD_EXCHANGE_NAME).durable(true).build();
  }

  @Bean
  public Binding deleteDeadBinding() {
    return BindingBuilder.bind(deleteDeadQueue())
        .to(deleteDeadExchange())
        .with(DELETE_DEAD_ROUTING_KEY)
        .noargs();
  }
}
