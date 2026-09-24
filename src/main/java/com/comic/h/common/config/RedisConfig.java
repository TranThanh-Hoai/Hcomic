package com.comic.h.common.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.comic.h.common.exception.CustomCacheErrorHandler;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

        @Override
        public CacheErrorHandler errorHandler() {
                return new CustomCacheErrorHandler();
        }

        private GenericJacksonJsonRedisSerializer createJsonRedisSerializer() {

                return GenericJacksonJsonRedisSerializer.builder()
                                .enableUnsafeDefaultTyping()
                                .enableSpringCacheNullValueSupport()
                                .build();
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

                StringRedisSerializer stringSerializer = new StringRedisSerializer();
                GenericJacksonJsonRedisSerializer jsonSerializer = createJsonRedisSerializer();

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Key lưu dưới dạng String dễ đọc
                template.setKeySerializer(stringSerializer);
                template.setHashKeySerializer(stringSerializer);

                // Value lưu dưới dạng JSON
                template.setValueSerializer(jsonSerializer);
                template.setHashValueSerializer(jsonSerializer);

                template.afterPropertiesSet();
                return template;
        }

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

                GenericJacksonJsonRedisSerializer jsonSerializer = createJsonRedisSerializer();
                StringRedisSerializer stringSerializer = new StringRedisSerializer();

                // Cấu hình Cache mặc định: TTL 10 phút, không lưu giá trị null, serialize bằng JSON
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonSerializer));

                // Cấu hình riêng biệt cho từng loại Cache phục vụ luồng đọc của Độc giả
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // 1. Cache trang chủ / phân trang truyện: TTL 10 phút
                cacheConfigurations.put("comics_page", defaultConfig.entryTtl(Duration.ofMinutes(10)));

                // 2. Cache chi tiết 1 bộ truyện (slug): TTL 30 phút
                cacheConfigurations.put("comic_detail", defaultConfig.entryTtl(Duration.ofMinutes(30)));

                // 3. Cache danh sách chương của bộ truyện (mục lục): TTL 30 phút
                cacheConfigurations.put("chapters_list", defaultConfig.entryTtl(Duration.ofMinutes(30)));

                // 4. Cache chi tiết 1 chương + danh sách link ảnh: TTL 60 phút
                cacheConfigurations.put("chapter_detail", defaultConfig.entryTtl(Duration.ofMinutes(60)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}
