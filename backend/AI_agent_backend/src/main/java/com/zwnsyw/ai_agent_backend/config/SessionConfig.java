package com.zwnsyw.ai_agent_backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.Duration;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400*30)
public class SessionConfig {

    // 配置Lettuce客户端参数
    @Bean
    public LettuceClientConfiguration redisSessionConfig() {
        return LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMinutes(30))
                .shutdownTimeout(Duration.ofSeconds(10)) // 添加关闭超时配置
                .build();
    }

    @Bean
    @Qualifier("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}