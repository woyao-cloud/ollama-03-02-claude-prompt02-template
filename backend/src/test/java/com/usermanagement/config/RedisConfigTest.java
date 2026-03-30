package com.usermanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisConfig 配置测试
 */
@SpringBootTest(classes = {RedisConfig.class, CacheProperties.class})
@DisplayName("RedisConfig 配置测试")
class RedisConfigTest {

    @Autowired
    private ApplicationContext context;

    @Nested
    @DisplayName("RedisTemplate 配置测试")
    class RedisTemplateTests {

        @Test
        @DisplayName("应该创建 RedisTemplate Bean")
        void shouldCreateRedisTemplateBean() {
            // Then
            assertThat(context.getBean(RedisTemplate.class)).isNotNull();
        }

        @Test
        @DisplayName("RedisTemplate 应该使用 String 作为 Key 序列化器")
        void shouldUseStringSerializerForKey() {
            // Given
            RedisTemplate<String, Object> redisTemplate = context.getBean(RedisTemplate.class);

            // Then
            assertThat(redisTemplate.getKeySerializer())
                .isInstanceOf(StringRedisSerializer.class);
        }

        @Test
        @DisplayName("RedisTemplate 应该使用 JSON 作为 Value 序列化器")
        void shouldUseJsonSerializerForValue() {
            // Given
            RedisTemplate<String, Object> redisTemplate = context.getBean(RedisTemplate.class);

            // Then
            assertThat(redisTemplate.getValueSerializer())
                .isInstanceOf(GenericJackson2JsonRedisSerializer.class);
        }
    }

    @Nested
    @DisplayName("ObjectMapper 测试")
    class ObjectMapperTests {

        @Test
        @DisplayName("应该创建 ObjectMapper Bean")
        void shouldCreateObjectMapperBean() {
            // Then
            assertThat(context.getBean(ObjectMapper.class)).isNotNull();
        }
    }
}
