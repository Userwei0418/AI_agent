package com.zwnsyw.ai_agent_backend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;


@Configuration
@Slf4j
public class RedisTemplateConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * 配置 RedisTemplate 使用 Jackson 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 键的序列化：纯文本
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // 值的序列化：JSON 格式
        GenericJackson2JsonRedisSerializer valueSerializer = genericJackson2JsonRedisSerializer();
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.setDefaultSerializer(valueSerializer);

        template.setEnableTransactionSupport(true); // 开启事务支持
        template.afterPropertiesSet();

        try {
            template.afterPropertiesSet();
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败：主机：{}，端口：{}", redisHost, redisPort);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "连接 Redis 失败");
        } catch (Exception e) {
            log.error("Redis 配置失败：主机：{}，端口：{}", redisHost, redisPort);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis 配置出错");
        }

        log.info("Redis 配置成功，主机：{}，端口：{}", redisHost, redisPort);

        return template;
    }

    /**
     * 配置通用 JSON 序列化器
     */
    private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        // 允许的业务包路径
                        .allowIfSubType("com.zwnsyw")
                        .allowIfSubType("com.zwnsyw.ai_agent_backend.*")
                        .allowIfSubType("com.zwnsyw.ai_agent_backend.dto.*")
                        .allowIfSubType("com.zwnsyw.ai_agent_backend.entity.*")
                        .allowIfSubType("com.zwnsyw.ai_agent_backend.vo.*")
                        // 允许的集合类型
                        .allowIfSubType("java.util.ArrayList")
                        .allowIfSubType("java.util.HashMap")
                        .allowIfSubType("java.util.Set")
                        .allowIfSubType("java.util.HashSet")
                        .allowIfSubType("java.util.LinkedList")
                        .allowIfSubType("java.util.TreeMap")
                        .allowIfSubType("java.util.LinkedHashMap")
                        // 允许基础类型和 Date
                        .allowIfSubType("java.lang.String")
                        .allowIfSubType("java.lang.Long")
                        .allowIfSubType("java.lang.Integer")
                        .allowIfSubType("java.util.Date")
                        // 其他类型（如 MyBatis 分页类）
                        .allowIfSubType("com.baomidou.mybatisplus.extension.plugins.pagination.Page")
                        .allowIfSubType("java.sql.Timestamp")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * 配置Cacheable注解的序列化器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        GenericJackson2JsonRedisSerializer valueSerializer = genericJackson2JsonRedisSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        RedisSerializer.string()
                ))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

}